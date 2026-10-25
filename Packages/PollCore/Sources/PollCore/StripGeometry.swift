import Foundation

public enum StripGeometry {
    /// Şeritteki parti sayısı.
    public static let columnCount = 8

    /// Parmağın altındaki sütun. Tasarımdaki `colFrom`: `floor(x / genişlik × 8)`, 0–7 aralığına sıkıştırılır.
    public static func column(at x: Double, width: Double, count: Int = columnCount) -> Int {
        guard count > 0, width > 0, x.isFinite else { return 0 }
        let raw = (x / width * Double(count)).rounded(.down)
        return Int(min(max(raw, 0), Double(count - 1)))
    }
}
