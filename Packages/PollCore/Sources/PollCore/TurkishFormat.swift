import Foundation

/// Türkçe sayı biçimleri. Cihaz diline bağlı kalmasın diye elle yazıldı.
public enum TurkishFormat {
    /// 16.4 → "%16,4"
    public static func percent(_ value: Double) -> String {
        "%" + String(format: "%.1f", value).replacingOccurrences(of: ".", with: ",")
    }

    /// 128460 → "128.460"
    public static func grouped(_ value: Int) -> String {
        let digits = String(value.magnitude)
        var out = ""
        for (index, digit) in digits.enumerated() {
            if index > 0, (digits.count - index) % 3 == 0 { out.append(".") }
            out.append(digit)
        }
        return value < 0 ? "-" + out : out
    }

    /// 128460 → "128.460 oy"
    public static func votes(_ count: Int) -> String { grouped(count) + " oy" }
}
