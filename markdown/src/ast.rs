//! Модель распарсенного Markdown-документа — зеркалит Kotlin sealed-иерархию
//! в `app/src/main/java/.../markdown/MarkdownAst.kt`. Обе стороны обязаны совпадать:
//! это единственный контракт между Rust и Kotlin (JSON — единственное, что летит
//! через границу JNI).

pub enum InlineNode {
    Text(String),
    Bold(Vec<InlineNode>),
    Italic(Vec<InlineNode>),
    /// Код НЕ разбирается дальше — по спеке Markdown это буквальный текст.
    Code(String),
}

pub enum BlockNode {
    Paragraph(Vec<InlineNode>),
    ThematicBreak,
}

/// Экранирование строки под JSON string literal. `"`, `\`, управляющие символы —
/// остальное (включая не-ASCII: кириллица, эмодзи) идёт как есть, JSON по спеке
/// обязан быть валиден в UTF-8 без дополнительного экранирования.
fn escape_json(input: &str) -> String {
    let mut out = String::with_capacity(input.len() + 2);
    for ch in input.chars() {
        match ch {
            '"' => out.push_str("\\\""),
            '\\' => out.push_str("\\\\"),
            '\n' => out.push_str("\\n"),
            '\r' => out.push_str("\\r"),
            '\t' => out.push_str("\\t"),
            c if (c as u32) < 0x20 => out.push_str(&format!("\\u{:04x}", c as u32)),
            c => out.push(c),
        }
    }
    out
}

impl InlineNode {
    fn to_json(&self) -> String {
        match self {
            InlineNode::Text(value) => {
                format!("{{\"type\":\"text\",\"value\":\"{}\"}}", escape_json(value))
            }
            InlineNode::Bold(children) => {
                format!("{{\"type\":\"bold\",\"children\":{}}}", inline_array_json(children))
            }
            InlineNode::Italic(children) => {
                format!("{{\"type\":\"italic\",\"children\":{}}}", inline_array_json(children))
            }
            InlineNode::Code(value) => {
                format!("{{\"type\":\"code\",\"value\":\"{}\"}}", escape_json(value))
            }
        }
    }
}

fn inline_array_json(nodes: &[InlineNode]) -> String {
    let parts: Vec<String> = nodes.iter().map(InlineNode::to_json).collect();
    format!("[{}]", parts.join(","))
}

impl BlockNode {
    fn to_json(&self) -> String {
        match self {
            BlockNode::Paragraph(inline) => {
                format!("{{\"type\":\"paragraph\",\"inline\":{}}}", inline_array_json(inline))
            }
            BlockNode::ThematicBreak => "{\"type\":\"thematic_break\"}".to_string(),
        }
    }
}

/// Единственная публичная функция модуля — сериализует весь документ в JSON-массив
/// блоков. Формат: см. markdown/README.md ("Контракт JSON").
pub fn blocks_to_json(blocks: &[BlockNode]) -> String {
    let parts: Vec<String> = blocks.iter().map(BlockNode::to_json).collect();
    format!("[{}]", parts.join(","))
}
