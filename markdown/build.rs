//! cmark-gfm (fork of `commonmark/cmark`)
//!
//!       |\      _,,,---,,_
//! ZZZzz /, \`.-'\`'    -.  ;-;;,_
//!      |,4-  ) )-,_. ,\` (  `'-'
//!     '---''(_/--'  \`-'\\_)
//!

use std::env;
use std::fs;
use std::path::PathBuf;

fn main() {
    let out_dir = PathBuf::from(env::var("OUT_DIR").expect("OUT_DIR не задан Cargo"));
    let manifest_dir = PathBuf::from(env::var("CARGO_MANIFEST_DIR").unwrap());
    let cmark_src = manifest_dir.join("cmark-gfm/src");

    write_generated_headers(&out_dir);

    let sources = [
        "arena.c",
        "blocks.c",
        "buffer.c",
        "cmark.c",
        "cmark_ctype.c",
        "commonmark.c",
        "footnotes.c",
        "houdini_href_e.c",
        "houdini_html_e.c",
        "houdini_html_u.c",
        "html.c",
        "inlines.c",
        "iterator.c",
        "latex.c",
        "linked_list.c",
        "man.c",
        "map.c",
        "node.c",
        "plaintext.c",
        "plugin.c",
        "references.c",
        "registry.c",
        "render.c",
        "scanners.c",
        "syntax_extension.c",
        "utf8.c",
        "xml.c",
    ];

    let mut build = cc::Build::new();
    build
        .include(&cmark_src)
        .include(&out_dir)
        .flag_if_supported("-Wno-unused-parameter")
        .flag_if_supported("-Wno-unused-function");

    for file in sources {
        build.file(cmark_src.join(file));
    }

    build.compile("cmark-gfm");

    println!("cargo:rerun-if-changed={}", cmark_src.display());
    println!("cargo:rerun-if-changed=build.rs");
}

fn write_generated_headers(out_dir: &PathBuf) {
    fs::write(
        out_dir.join("cmark-gfm_version.h"),
        r#"#ifndef CMARK_GFM_VERSION_H
#define CMARK_GFM_VERSION_H

#define CMARK_GFM_VERSION ((0 << 24) | (29 << 16) | (0 << 8) | 13)
#define CMARK_GFM_VERSION_STRING "0.29.0.gfm.13"

#endif
"#,
    )
    .expect("не удалось записать cmark-gfm_version.h");

    fs::write(
        out_dir.join("config.h"),
        r#"#ifndef CMARK_CONFIG_H
#define CMARK_CONFIG_H

#ifdef __cplusplus
extern "C" {
#endif

#define HAVE_STDBOOL_H
#include <stdbool.h>

#define HAVE___BUILTIN_EXPECT
#define HAVE___ATTRIBUTE__

#ifdef HAVE___ATTRIBUTE__
  #define CMARK_ATTRIBUTE(list) __attribute__ (list)
#else
  #define CMARK_ATTRIBUTE(list)
#endif

#ifndef CMARK_INLINE
  #define CMARK_INLINE inline
#endif

#ifdef __cplusplus
}
#endif

#endif
"#,
    )
    .expect("не удалось записать config.h");

    fs::write(
        out_dir.join("cmark-gfm_export.h"),
        r#"#ifndef CMARK_GFM_EXPORT_H
#define CMARK_GFM_EXPORT_H

#define CMARK_GFM_EXPORT
#define CMARK_GFM_NO_EXPORT

#endif
"#,
    )
    .expect("не удалось записать cmark-gfm_export.h");
}
