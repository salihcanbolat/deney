import SwiftUI
import DesignSystem
import PollCore

/// Kilit ekranı şeridi (Bölüm 5): basılı tut + kaydır, haptikler ve VoiceOver.
///
/// Durum (`held`, `activeIndex`) dışarıdan bağlanır; kilit ekranı yardım metnini, galeri de
/// kendi oynatıcısını aynı durumla yönetir.
struct StripView: View {
    /// Basılı tutma eşiği (Bölüm 5: 0,25–0,35 sn).
    static let holdDuration: Duration = .milliseconds(300)
    /// Eşikten önce parmak bu kadar kayarsa hareket kaydırma sayılır, şerit açılmaz.
    static let holdSlop: CGFloat = 10

    let data: StripData
    @Binding var held: Bool
    @Binding var activeIndex: Int
    var interactive = true

    @State private var isTouching = false
    @State private var touchX: CGFloat = 0
    @State private var holdTask: Task<Void, Never>?
    @State private var selectionTick = 0
    @AccessibilityFocusState private var focusedColumn: Int?
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    private var columnsWidth: CGFloat { StripMetrics.width - 16 }

    private var animation: Animation {
        reduceMotion ? .easeInOut(duration: 0.15) : Motion.candle
    }

    private var activeColumn: StripData.Column? {
        data.columns.indices.contains(activeIndex) ? data.columns[activeIndex] : nil
    }

    var body: some View {
        VStack(spacing: 8) {
            header
            columns
        }
        .padding(EdgeInsets(top: 11, leading: 8, bottom: 10, trailing: 8))
        .frame(width: StripMetrics.width)
        .modifier(StripGlass())
        .sensoryFeedback(.impact(weight: .light), trigger: held) { _, isHeld in isHeld && isTouching }
        .sensoryFeedback(.selection, trigger: selectionTick)
    }

    // MARK: Başlık

    private var header: some View {
        ZStack(alignment: .topLeading) {
            if held, let activeColumn {
                HeldHeader(column: activeColumn)
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .offset(y: -3)),
                        removal: .opacity
                    ))
            } else {
                RestHeader(title: data.title)
                    .transition(.opacity)
            }
        }
        .frame(maxWidth: .infinity, alignment: .topLeading)
    }

    // MARK: Sütunlar

    private var columns: some View {
        HStack(spacing: 0) {
            ForEach(Array(data.columns.enumerated()), id: \.element.id) { index, column in
                StripColumn(column: column, scale: data.scale, held: held, isActive: held && index == activeIndex)
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel(column.accessibilityLabel)
                    .accessibilityFocused($focusedColumn, equals: index)
                    .accessibilityAdjustableAction { direction in
                        switch direction {
                        case .increment: focusedColumn = min(index + 1, data.columns.count - 1)
                        case .decrement: focusedColumn = max(index - 1, 0)
                        @unknown default: break
                        }
                    }
            }
        }
        .frame(width: columnsWidth)
        .background(alignment: .topLeading) { spotlight }
        .contentShape(Rectangle())
        .gesture(pressAndDrag, including: interactive ? .all : .subviews)
        .onChange(of: focusedColumn) { _, column in
            // VoiceOver'da odaklanılan sütun basılı hâli gösterir.
            guard interactive, !isTouching else { return }
            withAnimation(animation) {
                if let column {
                    activeIndex = column
                    held = true
                } else {
                    held = false
                }
            }
        }
    }

    /// Basılı sütunun arkasında parmağı izleyen yumuşak bir ışık.
    private var spotlight: some View {
        let columnWidth = columnsWidth / CGFloat(max(data.columns.count, 1))
        let logoCenterY = StripMetrics.heldPositiveZone + StripMetrics.logoVerticalPadding + StripMetrics.logoSize / 2
        return Circle()
            .fill(RadialGradient(
                colors: [Palette.voteCandle.opacity(0.16), Palette.voteCandle.opacity(0)],
                center: .center, startRadius: 0, endRadius: 64
            ))
            .frame(width: 128, height: 128)
            .offset(x: columnWidth * (CGFloat(activeIndex) + 0.5) - 64, y: logoCenterY - 64)
            .opacity(held ? 1 : 0)
            .allowsHitTesting(false)
            .accessibilityHidden(true)
    }

    // MARK: Jest

    private var pressAndDrag: some Gesture {
        DragGesture(minimumDistance: 0, coordinateSpace: .local)
            .onChanged { value in
                touchX = value.location.x
                if !isTouching {
                    isTouching = true
                    startHoldTimer()
                }
                if held {
                    let column = StripGeometry.column(at: Double(value.location.x), width: Double(columnsWidth))
                    if column != activeIndex {
                        withAnimation(animation) { activeIndex = column }
                        selectionTick += 1
                    }
                } else if hypot(value.translation.width, value.translation.height) > Self.holdSlop {
                    holdTask?.cancel()
                }
            }
            .onEnded { _ in
                holdTask?.cancel()
                holdTask = nil
                isTouching = false
                if held {
                    withAnimation(animation) { held = false }
                }
            }
    }

    private func startHoldTimer() {
        holdTask?.cancel()
        holdTask = Task { @MainActor in
            try? await Task.sleep(for: Self.holdDuration)
            guard !Task.isCancelled, isTouching else { return }
            withAnimation(animation) {
                activeIndex = StripGeometry.column(at: Double(touchX), width: Double(columnsWidth))
                held = true
            }
        }
    }
}

// MARK: - Parçalar

/// Bir sütun: üstte artı mumlar, ortada logo (sıfır çizgisi), altta eksi mumlar.
struct StripColumn: View {
    let column: StripData.Column
    let scale: CandleScale
    let held: Bool
    let isActive: Bool

    var body: some View {
        let candleWidth = held ? StripMetrics.heldCandleWidth : StripMetrics.restCandleWidth
        VStack(spacing: 0) {
            VoteCandlePair(
                party: column.row.partyPos,
                leader: column.row.leaderPos,
                scale: scale,
                zoneHeight: held ? StripMetrics.heldPositiveZone : StripMetrics.restPositiveZone,
                candleWidth: candleWidth,
                glowing: isActive
            )
            PartyLogo(column.party, size: StripMetrics.logoSize, border: isActive ? .focusRing : .hairline)
                .animation(Motion.logoFocus) { $0.scaleEffect(isActive ? StripMetrics.activeLogoScale : 1) }
                .padding(.vertical, StripMetrics.logoVerticalPadding)
                .zIndex(1)
            VoteCandlePair(
                party: isActive ? column.row.partyNeg : nil,
                leader: isActive ? column.row.leaderNeg : nil,
                scale: scale,
                zoneHeight: held ? StripMetrics.heldNegativeZone : 0,
                candleWidth: candleWidth,
                direction: .down,
                glowing: isActive
            )
        }
        .frame(maxWidth: .infinity)
        .animation(Motion.fade) { $0.opacity(held && !isActive ? StripMetrics.dimmedColumnOpacity : 1) }
    }
}

/// Dinlenme hâlindeki başlık: anket adı ve "Canlı".
struct RestHeader: View {
    let title: String

    var body: some View {
        HStack {
            Text(title)
                .lineLimit(1)
            Spacer(minLength: 8)
            LiveBadge()
        }
        .font(LockFont.stripTitle)
        .foregroundStyle(Color.white.opacity(0.86))
        .padding(.horizontal, 6)
        .frame(height: 16)
    }
}

/// Basılı tutma hâlindeki başlık: parti, lider ve sıra; sağda oran tablosu.
struct HeldHeader: View {
    let column: StripData.Column

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(column.party.name)
                    .font(LockFont.partyName)
                    .foregroundStyle(.white)
                    .lineLimit(1)
                    .contentTransition(.opacity)
                Text("\(column.party.leaderName) · \(column.place). sıra")
                    .font(LockFont.leaderLine)
                    .monospacedDigit()
                    .foregroundStyle(Color.white.opacity(0.72))
                    .lineLimit(1)
                    .contentTransition(.opacity)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            // İsimler hızlı kaydırmada üst üste binmesin diye kısa ve net bir geçiş.
            .animation(.easeOut(duration: 0.12), value: column.id)
            RatioTable(row: column.row)
        }
        .padding(.horizontal, 6)
        .accessibilityHidden(true)
    }
}

/// ▲ / ▼ oran tablosu. Parmak başka partiye geçtiğinde rakamlar iOS sayaç animasyonuyla döner.
struct RatioTable: View {
    let row: PollResultRow

    var body: some View {
        Grid(horizontalSpacing: 6, verticalSpacing: 2) {
            GridRow {
                Color.clear.frame(width: 12, height: 1)
                heading("Parti")
                heading("Lider")
            }
            GridRow {
                arrow("▲")
                value(row.partyPos)
                value(row.leaderPos)
            }
            GridRow {
                arrow("▼")
                value(row.partyNeg)
                value(row.leaderNeg)
            }
        }
        .font(LockFont.table)
        .monospacedDigit()
    }

    private func heading(_ text: String) -> some View {
        Text(text)
            .font(LockFont.tableHeader)
            .foregroundStyle(Color.white.opacity(0.6))
            .frame(width: 38, alignment: .trailing)
    }

    private func arrow(_ text: String) -> some View {
        Text(text)
            .foregroundStyle(Color.white.opacity(0.6))
            .frame(width: 12, alignment: .leading)
    }

    private func value(_ number: Double?) -> some View {
        Text(number.map(TurkishFormat.percent) ?? "–")
            .foregroundStyle(Palette.voteCandle)
            .frame(width: 38, alignment: .trailing)
            .contentTransition(.numericText(value: number ?? 0))
    }
}

/// "Canlı" rozeti: kâğıt beyazı nokta, çevresinde yavaşça yayılan bir halka.
struct LiveBadge: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var pulsing = false

    var body: some View {
        HStack(spacing: 6) {
            ZStack {
                Circle()
                    .fill(Palette.voteCandle.opacity(0.18))
                    .frame(width: 12, height: 12)
                Circle()
                    .stroke(Palette.voteCandle.opacity(0.55), lineWidth: 1)
                    .frame(width: 6, height: 6)
                    .scaleEffect(pulsing ? 2.8 : 1)
                    .opacity(pulsing ? 0 : 0.9)
                Circle()
                    .fill(Palette.voteCandle)
                    .frame(width: 6, height: 6)
            }
            .frame(width: 6, height: 6)
            Text("Canlı")
        }
        .onAppear {
            guard !reduceMotion else { return }
            withAnimation(.easeOut(duration: 2).repeatForever(autoreverses: false)) {
                pulsing = true
            }
        }
    }
}

/// Şerit zemini. iOS 26'da sistemin Liquid Glass camı (kilit ekranı widget'larıyla aynı malzeme);
/// daha eski sürümlerde tasarımdaki %10 beyaz cam, arkasını bulanıklaştırarak.
struct StripGlass: ViewModifier {
    @ViewBuilder
    func body(content: Content) -> some View {
        let shape = RoundedRectangle(cornerRadius: StripMetrics.cornerRadius, style: .continuous)
        if #available(iOS 26.0, *) {
            content
                .glassEffect(.clear.tint(Color.white.opacity(0.05)), in: shape)
        } else {
            content.background {
                shape
                    .fill(.ultraThinMaterial)
                    .environment(\.colorScheme, .dark)
                    .opacity(0.2)
                    .overlay { shape.fill(Palette.stripGlassFill) }
                    .overlay { shape.strokeBorder(Palette.stripGlassStroke, lineWidth: 0.5) }
            }
        }
    }
}
