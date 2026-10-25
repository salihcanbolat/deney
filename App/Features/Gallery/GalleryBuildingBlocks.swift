import SwiftUI
import DesignSystem
import PollCore

/// Bir anketin şeritte gösterilen ilk 8 partisi ve ortak mum ölçeği.
struct StripSample {
    struct Column: Identifiable {
        let party: Party
        let row: PollResultRow
        var id: Party.ID { party.id }
    }

    let title: String
    let columns: [Column]
    let scale: CandleScale

    init(_ data: MockDataset, pollId: Poll.ID = "genel-secim") {
        let rows = data.result(for: pollId)?.top(StripGeometry.columnCount) ?? []
        title = data.poll(pollId)?.title ?? ""
        columns = rows.compactMap { row in data.party(row.partyId).map { Column(party: $0, row: row) } }
        scale = CandleScale(rows: rows)
    }
}

/// iOS gruplu liste bölümü: büyük harfli başlık, beyaz kart, isteğe bağlı açıklama.
struct GroupedSection<Content: View>: View {
    let title: String
    var footer: String? = nil
    @ViewBuilder var content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(title)
                .font(AppFont.footnote)
                .tracking(0.2)
                .foregroundStyle(Palette.secondaryText)
                .padding(.leading, 16)
                .padding(.bottom, 7)
            content
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Palette.card)
                .clipShape(RoundedRectangle(cornerRadius: Metrics.cardRadius, style: .continuous))
            if let footer {
                Text(footer)
                    .font(AppFont.footnote)
                    .foregroundStyle(Palette.secondaryText)
                    .padding(.horizontal, 16)
                    .padding(.top, 8)
            }
        }
    }
}

/// Kilit ekranı zemininde koyu bir alan.
struct LockBackdrop<Content: View>: View {
    @ViewBuilder var content: Content

    var body: some View {
        content
            .padding(.vertical, 14)
            .frame(maxWidth: .infinity)
            .background(Palette.lockWallpaper)
    }
}

/// Cam görünümlü şerit kabı ve dinlenme hâlindeki başlık satırı.
struct StripPanel<Content: View>: View {
    let title: String
    @ViewBuilder var content: Content

    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Text(title)
                Spacer(minLength: 8)
                HStack(spacing: 6) {
                    Circle()
                        .fill(Palette.voteCandle)
                        .frame(width: 6, height: 6)
                        .background {
                            Circle()
                                .fill(Palette.voteCandle.opacity(0.18))
                                .frame(width: 12, height: 12)
                        }
                    Text("Canlı")
                }
            }
            .font(LockFont.stripTitle)
            .foregroundStyle(Color.white.opacity(0.86))
            .padding(.horizontal, 6)
            .frame(height: 16)
            content
        }
        .padding(EdgeInsets(top: 11, leading: 8, bottom: 10, trailing: 8))
        .frame(width: StripMetrics.width)
        .background {
            RoundedRectangle(cornerRadius: StripMetrics.cornerRadius, style: .continuous)
                .fill(Palette.stripGlassFill)
                .overlay {
                    RoundedRectangle(cornerRadius: StripMetrics.cornerRadius, style: .continuous)
                        .strokeBorder(Palette.stripGlassStroke, lineWidth: 0.5)
                }
        }
    }
}

/// Şeridin sütunları: üstte artı mumlar, ortada logo, altta eksi mumlar.
/// Aşama 2'deki gerçek şeridin görsel temeli; burada jest yok, durum dışarıdan verilir.
struct StripColumnsPreview: View {
    let columns: [StripSample.Column]
    let scale: CandleScale
    let held: Bool
    let activeIndex: Int

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(columns.enumerated()), id: \.element.id) { index, column in
                columnView(column, isActive: held && index == activeIndex)
            }
        }
    }

    private func columnView(_ column: StripSample.Column, isActive: Bool) -> some View {
        let candleWidth = held ? StripMetrics.heldCandleWidth : StripMetrics.restCandleWidth
        return VStack(spacing: 0) {
            VoteCandlePair(
                party: column.row.partyPos,
                leader: column.row.leaderPos,
                scale: scale,
                zoneHeight: held ? StripMetrics.heldPositiveZone : StripMetrics.restPositiveZone,
                candleWidth: candleWidth
            )
            PartyLogo(column.party, size: StripMetrics.logoSize, border: isActive ? .focusRing : .hairline)
                .animation(Motion.logoFocus) { $0.scaleEffect(isActive ? StripMetrics.activeLogoScale : 1) }
                .padding(.vertical, StripMetrics.logoVerticalPadding)
            VoteCandlePair(
                party: isActive ? column.row.partyNeg : nil,
                leader: isActive ? column.row.leaderNeg : nil,
                scale: scale,
                zoneHeight: held ? StripMetrics.heldNegativeZone : 0,
                candleWidth: candleWidth,
                direction: .down
            )
        }
        .frame(maxWidth: .infinity)
        .animation(Motion.fade) { $0.opacity(held && !isActive ? StripMetrics.dimmedColumnOpacity : 1) }
    }
}
