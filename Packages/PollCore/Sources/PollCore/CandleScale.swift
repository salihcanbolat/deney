import Foundation

/// Tüm mumların ortak ölçeği; partiler birbiriyle karşılaştırılabilsin diye tek bir tavan kullanılır.
///
/// Tavan, gösterilen satırlardaki en yüksek değerin bir üst tam sayısıdır. Örnek veride en yüksek
/// değer %26,9 olduğu için tavan 27 olur; bu, tasarımdaki `MAX = 27` ile aynıdır.
public struct CandleScale: Hashable, Sendable {
    public let ceiling: Double

    public init(ceiling: Double) {
        self.ceiling = max(ceiling, 1)
    }

    public init<Rows: Sequence>(rows: Rows) where Rows.Element == PollResultRow {
        let peak = rows.flatMap(\.allValues).max() ?? 1
        self.init(ceiling: peak.rounded(.up))
    }

    /// Mum yüksekliği (pt). Tasarımdaki formül: `max(2, değer / tavan × (alan − 2))`.
    /// Değer yoksa ya da alan kapalıysa (eksi mumların dinlenme hâli) yükseklik 0'dır.
    public func height(for value: Double?, in zone: Double) -> Double {
        guard let value, zone > 0 else { return 0 }
        return max(2, value / ceiling * (zone - 2))
    }
}
