import Foundation
import PollCore

/// Şeritte gösterilen anket: sıralamadaki ilk 8 parti ve ortak mum ölçeği.
struct StripData {
    struct Column: Identifiable {
        let party: Party
        let row: PollResultRow
        /// Sıralamadaki yeri, 1'den başlar.
        let place: Int
        /// Sonuçlar sembolikse oran okunmaz, gösterilmez.
        let isSymbolic: Bool
        var id: Party.ID { party.id }

        /// "Parti adı: parti %x, lider %y; olumsuz parti %z, lider %w" (Bölüm 5, Erişilebilirlik).
        var accessibilityLabel: String {
            if isSymbolic { return party.name + ": sembolik veri, oy oranı yok" }
            let positive = Self.pair("parti", row.partyPos, "lider", row.leaderPos)
            let negative = Self.pair("olumsuz parti", row.partyNeg, "lider", row.leaderNeg)
            return party.name + ": " + [positive, negative].compactMap { $0 }.joined(separator: "; ")
        }

        private static func pair(_ first: String, _ a: Double?, _ second: String, _ b: Double?) -> String? {
            let parts = [a.map { "\(first) \(TurkishFormat.percent($0))" }, b.map { "\(second) \(TurkishFormat.percent($0))" }]
            let text = parts.compactMap { $0 }.joined(separator: ", ")
            return text.isEmpty ? nil : text
        }
    }

    let title: String
    let columns: [Column]
    let scale: CandleScale
    let isSymbolic: Bool

    init(_ data: MockDataset, pollId: Poll.ID = "genel-secim") {
        let result = data.result(for: pollId)
        let rows = result?.top(StripGeometry.columnCount) ?? []
        let symbolic = result?.isSymbolic ?? false
        title = data.poll(pollId)?.title ?? ""
        isSymbolic = symbolic
        columns = rows.enumerated().compactMap { index, row in
            data.party(row.partyId).map { Column(party: $0, row: row, place: index + 1, isSymbolic: symbolic) }
        }
        // Sembolik eşit değerler alanın yarısına kadar uzanır; dolu bir duvar gibi görünmez.
        let fitted = CandleScale(rows: rows)
        scale = symbolic ? CandleScale(ceiling: fitted.ceiling * 2) : fitted
    }
}
