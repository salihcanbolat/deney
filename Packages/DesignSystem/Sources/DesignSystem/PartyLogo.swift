import SwiftUI
import UIKit
import PollCore

/// Parti logosu. `party.logoAsset` doluysa ve asset kataloğunda varsa gerçek logo çizilir;
/// yoksa parti renginde bir daire ve kodun ilk harfi gösterilir. Gerçek logolar geldiğinde
/// yalnızca veri değişir, ekranlar değişmez.
public struct PartyLogo: View {
    public enum Border: Equatable, Sendable {
        case none
        /// Kilit ekranındaki ince, yarı saydam kenar.
        case hairline
        /// Basılı tutulan logonun 2 pt beyaz halkası.
        case focusRing
    }

    private let party: Party
    private let size: CGFloat
    private let border: Border

    public init(_ party: Party, size: CGFloat, border: Border = .none) {
        self.party = party
        self.size = size
        self.border = border
    }

    public var body: some View {
        mark
            .frame(width: size, height: size)
            .clipShape(Circle())
            .overlay {
                Circle()
                    .strokeBorder(Color.white.opacity(0.35), lineWidth: 0.5)
                    .opacity(border == .hairline ? 1 : 0)
            }
            .overlay {
                Circle()
                    .strokeBorder(Color.white.opacity(0.95), lineWidth: 2)
                    .padding(-2)
                    .opacity(border == .focusRing ? 1 : 0)
            }
            .accessibilityElement()
            .accessibilityLabel(party.name)
    }

    @ViewBuilder
    private var mark: some View {
        if let asset = party.logoAsset, let image = UIImage(named: asset) {
            Image(uiImage: image)
                .resizable()
                .scaledToFill()
                .overlay { rim }
        } else {
            coin
        }
    }

    /// Yer tutucu rozet: parti renginde, sol üstten ışık alan bir madeni para.
    private var coin: some View {
        let base = RGBA(hex: party.color)
        return Circle()
            .fill(RadialGradient(
                colors: [
                    base.mixed(with: .white, amount: 0.24).color,
                    base.color,
                    base.mixed(with: .black, amount: 0.28).color,
                ],
                center: UnitPoint(x: 0.3, y: 0.22),
                startRadius: 0,
                endRadius: size * 0.9
            ))
            .overlay { rim }
            .overlay {
                Text(party.monogram)
                    .font(.system(size: Self.monogramSize(for: size), weight: .heavy, design: .rounded))
                    .foregroundStyle(.white)
                    .shadow(color: .black.opacity(0.28), radius: 0.5, y: 0.5)
            }
    }

    /// Üst kenarda ışık, alt kenarda gölge: logoya kabartma hissi verir.
    private var rim: some View {
        Circle()
            .strokeBorder(
                LinearGradient(
                    colors: [.white.opacity(0.55), .white.opacity(0.06), .black.opacity(0.18)],
                    startPoint: .top,
                    endPoint: .bottom
                ),
                lineWidth: max(0.75, size * 0.035)
            )
    }

    /// Tasarımdaki harf boyutları: 20 pt logoda 9, 26 pt'de 12, 40 pt'de 17.
    static func monogramSize(for size: CGFloat) -> CGFloat {
        switch size {
        case ..<23: (size * 0.45).rounded()
        case ..<33: (size * 0.4615).rounded()
        default: (size * 0.425).rounded()
        }
    }
}
