//!       |\      _,,,---,,_
//! ZZZzz /, \`.-'\`'    -.  ;-;;,_
//!      |,4-  ) )-,_. ,\` (  `'-'
//!     '---''(_/--'  \`-'\\_)
//!
//! Тонкий FFI-биндинг к cmark-gfm (C, сурсы в cmark-gfm/, собираются в build.rs).

use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_int, c_void};
use std::sync::Once;

#[repr(C)]
struct CmarkParser {
    _private: [u8; 0],
}
#[repr(C)]
struct CmarkNode {
    _private: [u8; 0],
}
#[repr(C)]
struct CmarkSyntaxExtension {
    _private: [u8; 0],
}
#[repr(C)]
struct CmarkLlist {
    _private: [u8; 0],
}

extern "C" {
    fn cmark_gfm_core_extensions_ensure_registered();

    // src/cmark-gfm-extension_api.h
    fn cmark_find_syntax_extension(name: *const c_char) -> *mut CmarkSyntaxExtension;
    fn cmark_parser_attach_syntax_extension(
        parser: *mut CmarkParser,
        extension: *mut CmarkSyntaxExtension,
    ) -> c_int;
    fn cmark_parser_get_syntax_extensions(parser: *mut CmarkParser) -> *mut CmarkLlist;

    // src/cmark-gfm.h
    fn cmark_parser_new(options: c_int) -> *mut CmarkParser;
    fn cmark_parser_free(parser: *mut CmarkParser);
    fn cmark_parser_feed(parser: *mut CmarkParser, buffer: *const c_char, len: usize);
    fn cmark_parser_finish(parser: *mut CmarkParser) -> *mut CmarkNode;
    fn cmark_node_free(node: *mut CmarkNode);
    fn cmark_render_html(
        root: *mut CmarkNode,
        options: c_int,
        extensions: *mut CmarkLlist,
    ) -> *mut c_char;

    fn free(ptr: *mut c_void);
}

const CMARK_OPT_UNSAFE: c_int = 1 << 17;

const ENABLED_EXTENSIONS: &[&str] = &["table", "strikethrough"];

static REGISTER_EXTENSIONS: Once = Once::new();

pub fn render_html(markdown: &str) -> Option<String> {
    REGISTER_EXTENSIONS.call_once(|| unsafe { cmark_gfm_core_extensions_ensure_registered() });

    let bytes = markdown.as_bytes();

    unsafe {
        let parser = cmark_parser_new(CMARK_OPT_UNSAFE);
        if parser.is_null() {
            return None;
        }

        for name in ENABLED_EXTENSIONS {
            let c_name = CString::new(*name).expect("имя расширения содержит NUL-байт");
            let extension = cmark_find_syntax_extension(c_name.as_ptr());
            if extension.is_null() {
                continue;
            }
            cmark_parser_attach_syntax_extension(parser, extension);
        }

        cmark_parser_feed(parser, bytes.as_ptr() as *const c_char, bytes.len());
        let doc = cmark_parser_finish(parser);

        if doc.is_null() {
            cmark_parser_free(parser);
            return None;
        }

        let extensions = cmark_parser_get_syntax_extensions(parser);
        let html_ptr = cmark_render_html(doc, CMARK_OPT_UNSAFE, extensions);

        cmark_node_free(doc);
        cmark_parser_free(parser);

        if html_ptr.is_null() {
            return None;
        }

        let html = CStr::from_ptr(html_ptr).to_string_lossy().into_owned();
        free(html_ptr as *mut c_void);
        Some(html)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn renders_bold_italic_code() {
        let html = render_html("**bold** and *italic* and `code`").unwrap();
        assert!(html.contains("<strong>bold</strong>"), "html: {html}");
        assert!(html.contains("<em>italic</em>"), "html: {html}");
        assert!(html.contains("<code>code</code>"), "html: {html}");
    }

    #[test]
    fn renders_thematic_break() {
        let html = render_html("before\n\n---\n\nafter").unwrap();
        assert!(html.contains("<hr"), "html: {html}");
    }

    #[test]
    fn passes_raw_html_in_unsafe_mode() {
        let html = render_html("<img src=\"/home/test.png\" alt=\"image\" />").unwrap();
        assert!(html.contains("<img src=\"/home/test.png\""), "html: {html}");
    }

    #[test]
    fn renders_markdown_image() {
        let html = render_html("![image](/home/test.png)").unwrap();
        assert!(html.contains("<img src=\"/home/test.png\" alt=\"image\" />"), "html: {html}");
    }

    #[test]
    fn renders_gfm_table() {
        let html = render_html("| a | b |\n|---|:-:|\n| 1 | 2 |\n").unwrap();
        assert!(html.contains("<table>"), "html: {html}");
        assert!(html.contains("<th>a</th>"), "html: {html}");
        assert!(html.contains("<th align=\"center\">b</th>"), "html: {html}");
        assert!(html.contains("<td>1</td>"), "html: {html}");
        assert!(html.contains("<td align=\"center\">2</td>"), "html: {html}");
    }

    #[test]
    fn renders_strikethrough() {
        let html = render_html("this is ~~gone~~ text").unwrap();
        assert!(html.contains("<del>gone</del>"), "html: {html}");
    }

    #[test]
    fn table_and_strikethrough_alongside_regular_markdown() {
        let html = render_html("# Heading\n\n**bold** ~~strike~~\n\n| L | R |\n|:--|--:|\n| a | b |\n")
            .unwrap();
        assert!(html.contains("<h1>Heading</h1>"), "html: {html}");
        assert!(html.contains("<strong>bold</strong>"), "html: {html}");
        assert!(html.contains("<del>strike</del>"), "html: {html}");
        assert!(html.contains("<table>"), "html: {html}");
    }
}
