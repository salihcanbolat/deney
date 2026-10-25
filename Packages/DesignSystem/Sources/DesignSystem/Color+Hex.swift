import SwiftUI
import UIKit

struct RGBA {
    var red: Double
    var green: Double
    var blue: Double
    var alpha: Double

    /// "#RRGGBB" ya da "#RRGGBBAA". Geçersiz değer siyaha düşer.
    init(hex: String) {
        var string = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        if string.hasPrefix("#") { string.removeFirst() }
        let value = UInt64(string, radix: 16) ?? 0
        switch string.count {
        case 8:
            red = Double((value >> 24) & 0xFF) / 255
            green = Double((value >> 16) & 0xFF) / 255
            blue = Double((value >> 8) & 0xFF) / 255
            alpha = Double(value & 0xFF) / 255
        default:
            red = Double((value >> 16) & 0xFF) / 255
            green = Double((value >> 8) & 0xFF) / 255
            blue = Double(value & 0xFF) / 255
            alpha = 1
        }
    }
}

public extension Color {
    init(hex: String) {
        let rgba = RGBA(hex: hex)
        self.init(.sRGB, red: rgba.red, green: rgba.green, blue: rgba.blue, opacity: rgba.alpha)
    }

    /// Açık ve koyu modda farklı değer alan renk.
    static func adaptive(light: String, dark: String) -> Color {
        Color(uiColor: UIColor { traits in
            UIColor(hex: traits.userInterfaceStyle == .dark ? dark : light)
        })
    }
}

public extension UIColor {
    convenience init(hex: String) {
        let rgba = RGBA(hex: hex)
        self.init(red: rgba.red, green: rgba.green, blue: rgba.blue, alpha: rgba.alpha)
    }
}
