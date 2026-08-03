//! Native libmarkdown.so parser

/*
 *       |\      _,,,---,,_
 * ZZZzz /, \`.-'\`'    -.  ;-;;,_
 *      |,4-  ) )-,_. ,\` (  `'-'
 *     '---''(_/--'  \`-'\\_)
 *
 *  RichNote
 *    rich_beluga, 2026
 */

mod ast;
mod parser;

use jni::objects::{JClass, JString};
use jni::strings::JNIString;
use jni::sys::{jint, jstring, JNI_ERR, JNI_VERSION_1_6};
use jni::{JNIEnv, JavaVM, NativeMethod};
use std::os::raw::c_void;

const NATIVE_CLASS: &str = "com/rich_beluga/richnote/markdown/MarkdownNative";

#[no_mangle]
pub extern "system" fn JNI_OnLoad(vm: JavaVM, _reserved: *mut c_void) -> jint {
    let mut env = match vm.get_env() {
        Ok(env) => env,
        Err(_) => return JNI_ERR,
    };

    let class = match env.find_class(NATIVE_CLASS) {
        Ok(class) => class,
        Err(_) => return JNI_ERR,
    };

    let methods = [NativeMethod {
        name: JNIString::from("parseMarkdown"),
        sig: JNIString::from("(Ljava/lang/String;)Ljava/lang/String;"),
        fn_ptr: parse_markdown as *mut c_void,
    }];

    if env.register_native_methods(&class, &methods).is_err() {
        return JNI_ERR;
    }

    JNI_VERSION_1_6
}

extern "system" fn parse_markdown<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    source: JString<'local>,
) -> jstring {
    let input: String = match env.get_string(&source) {
        Ok(s) => s.into(),
        Err(_) => return std::ptr::null_mut(),
    };

    let json = match std::panic::catch_unwind(|| {
        let blocks = parser::parse(&input);
        ast::blocks_to_json(&blocks)
    }) {
        Ok(json) => json,
        Err(_) => return std::ptr::null_mut(),
    };

    match env.new_string(json) {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}
