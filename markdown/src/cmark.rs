//!       |\      _,,,---,,_
//! ZZZzz /, \`.-'\`'    -.  ;-;;,_
//!      |,4-  ) )-,_. ,\` (  `'-'
//!     '---''(_/--'  \`-'\\_)
//!
//! Тонкий FFI-биндинг к cmark-gfm (C, сурсы в cmark-gfm/, собираются в build.rs).
//! Используется ровно одна функция из публичного API — `cmark_markdown_to_html`
//! (см. cmark-gfm/src/cmark-gfm.h) — она делает parse+render одним вызовом,
//! никакого промежуточного AST на Rust-стороне больше нет.

use std::ffi::CStr;
use std::os::raw::{c_char, c_int, c_void};

extern "C" {
    fn cmark_markdown_to_html(text: *const c_char, len: usize, options: c_int) -> *mut c_char;

    fn free(ptr: *mut c_void);
}

/// CMARK_OPT_DEFAULT (0) | CMARK_OPT_SAFE (1<<3) — безопасный рендер: сырой
/// HTML и опасные ссылки (javascript: и т.п.) во входном markdown экранируются,
/// а не проходят в вывод как есть.
const CMARK_OPT_SAFE: c_int = 1 << 3;

/// Рендерит markdown в HTML через cmark-gfm. None — если cmark вернул null
/// (по факту не должно происходить при валидном UTF-8 на входе, но граница
/// FFI — не то место, где стоит паниковать на всякий случай).
pub fn render_html(markdown: &str) -> Option<String> {
    let bytes = markdown.as_bytes();

    let html_ptr =
        unsafe { cmark_markdown_to_html(bytes.as_ptr() as *const c_char, bytes.len(), CMARK_OPT_SAFE) };

    if html_ptr.is_null() {
        return None;
    }

    let html = unsafe { CStr::from_ptr(html_ptr) }.to_string_lossy().into_owned();
    unsafe { free(html_ptr as *mut c_void) };

    Some(html)
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
    fn escapes_raw_html_in_safe_mode() {
        let html = render_html("<script>alert(1)</script>").unwrap();
        assert!(!html.contains("<script>"), "html: {html}");
    }
}
