package org.wirelessredstone.model

enum class TriggerCondition {
    ON_STEP,      // Giẫm lên block
    ON_CLICK,     // Click chuột phải
    ON_BREAK,     // Đập vỡ block
    ON_REDSTONE   // Nhận điện Redstone từ ngoài (Nút, cần gạt...)
}