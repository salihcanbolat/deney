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

extension RGBA {
    static let white = RGBA(hex: "#FFFFFF")
    static let black = RGBA(hex: "#000000")

    /// İki renk arasında doğrusal karışım (`amount` 0 → self, 1 → other).
    func mixed(with other: RGBA, amount: Double) -> RGBA {
        var out = self
        out.red += (other.red - red) * amount
        out.green += (other.green - green) * amount
        out.blue += (other.blue - blue) * amount
        return out
    }

    var color: Color { Color(.sRGB, red: red, green: green, blue: blue, opacity: alpha) }
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
