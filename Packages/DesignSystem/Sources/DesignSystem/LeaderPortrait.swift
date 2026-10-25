import SwiftUI
import UIKit
import PollCore

/// Lider fotoğrafı yuvası (pusulada 42 × 50 pt). `party.leaderPhotoAssets` doluysa ve asset
/// kataloğunda varsa fotoğraf gösterilir; yoksa kâğıt tonunda bir kartta baş harfler.
public struct LeaderPortrait: View {
    private let party: Party
    private let size: CGSize

    public init(_ party: Party, size: CGSize = CGSize(width: 42, height: 50)) {
        self.party = party
        self.size = size
    }

    public var body: some View {
        let shape = RoundedRectangle(cornerRadius: 5, style: .continuous)
        content
            .frame(width: size.width, height: size.height)
            .clipShape(shape)
            .overlay { shape.strokeBorder(Palette.photoSlotEdge, lineWidth: 0.5) }
            .accessibilityElement()
            .accessibilityLabel(party.leaderName)
    }

    @ViewBuilder
    private var content: some View {
        if let asset = party.leaderPhotoAssets?.first, let image = UIImage(named: asset) {
            Image(uiImage: image)
                .resizable()
                .scaledToFill()
        } else {
            Palette.photoSlot
                .overlay {
                    Text(party.leaderInitials)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(Palette.paperInkSecondary)
                }
        }
    }
}
