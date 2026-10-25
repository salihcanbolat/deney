import SwiftUI
import PollCore

/// Bitişik iki mum: solda parti oyu, sağda liderin oyu.
///
/// Logo sıfır çizgisidir: `.up` mumlar logonun üstünde yukarı, `.down` (eksi) mumlar altında
/// aşağı uzanır. Renk her zaman `Palette.voteCandle`'dır; parti renkleri mumlarda kullanılmaz.
/// Yükseklik ve genişlik değişimleri bileşenin içinde yay eğrisiyle canlandırılır.
public struct VoteCandlePair: View {
    public enum Direction: Sendable {
        case up
        case down
    }

    private let party: Double?
    private let leader: Double?
    private let scale: CandleScale
    private let zoneHeight: CGFloat
    private let candleWidth: CGFloat
    private let direction: Direction
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    /// - Parameters:
    ///   - party: Parti oyu (%). `nil` ise mum çizilmez.
    ///   - leader: Liderin oyu (%). `nil` ise mum çizilmez.
    ///   - zoneHeight: Mumların uzanabildiği alan. 0 verilirse çift kapanır.
    public init(party: Double?, leader: Double?, scale: CandleScale, zoneHeight: CGFloat, candleWidth: CGFloat, direction: Direction = .up) {
        self.party = party
        self.leader = leader
        self.scale = scale
        self.zoneHeight = zoneHeight
        self.candleWidth = candleWidth
        self.direction = direction
    }

    public var body: some View {
        let partyHeight = height(for: party)
        let leaderHeight = height(for: leader)
        HStack(alignment: direction == .up ? .bottom : .top, spacing: StripMetrics.candleGap) {
            candle(height: partyHeight)
            candle(height: leaderHeight)
        }
        .frame(maxWidth: .infinity)
        .frame(height: zoneHeight, alignment: direction == .up ? .bottom : .top)
        .animation(reduceMotion ? .easeInOut(duration: 0.15) : Motion.candle, value: [partyHeight, leaderHeight, zoneHeight, candleWidth])
        .accessibilityHidden(true)
    }

    private func height(for value: Double?) -> CGFloat {
        CGFloat(scale.height(for: value, in: Double(zoneHeight)))
    }

    private func candle(height: CGFloat) -> some View {
        UnevenRoundedRectangle(cornerRadii: cornerRadii, style: .circular)
            .fill(Palette.voteCandle)
            .frame(width: candleWidth, height: height)
    }

    /// Yukarı mumlarda üst köşeler 3, alt köşeler 1 pt; eksi mumlarda tersi.
    private var cornerRadii: RectangleCornerRadii {
        switch direction {
        case .up: RectangleCornerRadii(topLeading: 3, bottomLeading: 1, bottomTrailing: 1, topTrailing: 3)
        case .down: RectangleCornerRadii(topLeading: 1, bottomLeading: 3, bottomTrailing: 3, topTrailing: 1)
        }
    }
}
