//! Минимальный Markdown-парсер — НЕ полная реализация CommonMark, это перенос
//! исходной Kotlin-версии на native. Понимает:
//!  - **bold**
//!  - *italic*
//!  - `code` (литерально, без вложенного разбора)
//!  - --- / *** / ___ на всю строку (≥3 повтора одного символа, только пробелы вокруг)
//!
//! Сознательно НЕ сделано (как и раньше): экранирование (`\*`), заголовки, списки,
//! ссылки, code fences, бэктрекинг при некорректно закрытых `*`/`**`.

use crate::ast::{BlockNode, InlineNode};

/// Блочный разбор: построчно группирует текст в параграфы, разделённые пустыми
/// строками или строкой-разделителем.
pub fn parse(source: &str) -> Vec<BlockNode> {
    let mut blocks = Vec::new();
    let mut paragraph_lines: Vec<&str> = Vec::new();

    for line in source.lines() {
        if is_thematic_break(line) {
            flush_paragraph(&mut paragraph_lines, &mut blocks);
            blocks.push(BlockNode::ThematicBreak);
        } else if line.trim().is_empty() {
            flush_paragraph(&mut paragraph_lines, &mut blocks);
        } else {
            paragraph_lines.push(line);
        }
    }
    flush_paragraph(&mut paragraph_lines, &mut blocks);
    blocks
}

fn flush_paragraph(paragraph_lines: &mut Vec<&str>, blocks: &mut Vec<BlockNode>) {
    if paragraph_lines.is_empty() {
        return;
    }
    let text = paragraph_lines.join("\n");
    blocks.push(BlockNode::Paragraph(parse_inline(&text)));
    paragraph_lines.clear();
}

fn is_thematic_break(line: &str) -> bool {
    let trimmed = line.trim();
    if trimmed.chars().count() < 3 {
        return false;
    }
    for marker in ['-', '*', '_'] {
        let all_marker_or_space = trimmed.chars().all(|c| c == marker || c == ' ');
        let marker_count = trimmed.chars().filter(|&c| c == marker).count();
        if all_marker_or_space && marker_count >= 3 {
            return true;
        }
    }
    false
}

/// Инлайн-разбор: линейное сканирование с рекурсией внутрь bold/italic (чтобы
/// `**bold *and italic* text**` тоже работал), без рекурсии внутрь code.
/// Разделители `*`/`` ` `` — однобайтовые ASCII-символы, поэтому байтовые срезы
/// по их позициям всегда попадают на границу UTF-8 символа (не ломают кириллицу
/// и прочий не-ASCII текст вокруг них).
pub fn parse_inline(text: &str) -> Vec<InlineNode> {
    let mut nodes = Vec::new();
    let mut buffer = String::new();
    let mut i = 0usize;

    while i < text.len() {
        if text[i..].starts_with("**") {
            if let Some(rel_close) = text[i + 2..].find("**") {
                flush_text(&mut buffer, &mut nodes);
                let inner = &text[i + 2..i + 2 + rel_close];
                nodes.push(InlineNode::Bold(parse_inline(inner)));
                i = i + 2 + rel_close + 2;
                continue;
            }
        } else if text[i..].starts_with('*') {
            if let Some(rel_close) = text[i + 1..].find('*') {
                flush_text(&mut buffer, &mut nodes);
                let inner = &text[i + 1..i + 1 + rel_close];
                nodes.push(InlineNode::Italic(parse_inline(inner)));
                i = i + 1 + rel_close + 1;
                continue;
            }
        } else if text[i..].starts_with('`') {
            if let Some(rel_close) = text[i + 1..].find('`') {
                flush_text(&mut buffer, &mut nodes);
                let inner = &text[i + 1..i + 1 + rel_close];
                nodes.push(InlineNode::Code(inner.to_string()));
                i = i + 1 + rel_close + 1;
                continue;
            }
        }

        // Не разделитель или закрывающая пара не нашлась — один char (не байт!)
        // в текстовый буфер и дальше.
        let ch = text[i..].chars().next().unwrap();
        buffer.push(ch);
        i += ch.len_utf8();
    }

    flush_text(&mut buffer, &mut nodes);
    nodes
}

fn flush_text(buffer: &mut String, nodes: &mut Vec<InlineNode>) {
    if buffer.is_empty() {
        return;
    }
    nodes.push(InlineNode::Text(std::mem::take(buffer)));
}

#[cfg(test)]
mod tests {
    use super::*;

    // `cargo test` гоняется на хосте (не нужен Android NDK) — быстрая проверка
    // логики без пересборки .so и без эмулятора/устройства.

    #[test]
    fn thematic_break_variants() {
        assert!(is_thematic_break("---"));
        assert!(is_thematic_break("***"));
        assert!(is_thematic_break("___"));
        assert!(is_thematic_break("- - -"));
        assert!(!is_thematic_break("--"));
        assert!(!is_thematic_break("hello"));
    }

    #[test]
    fn bold_and_italic_and_code() {
        let nodes = parse_inline("**bold** *italic* `code`");
        assert_eq!(nodes.len(), 5); // Bold, " ", Italic, " ", Code
    }

    #[test]
    fn unicode_safe() {
        // Кириллица вокруг ASCII-разделителей не должна паниковать на срезах.
        let nodes = parse_inline("Привет **жирный** мир, código `код`");
        assert!(!nodes.is_empty());
    }
}
