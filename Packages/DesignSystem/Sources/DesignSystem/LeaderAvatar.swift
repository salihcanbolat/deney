import SwiftUI
import UIKit
import PollCore

/// Yuvarlak lider fotoğrafı. Eş genel başkanlı partilerde iki fotoğraf hafifçe üst üste biner.
/// Fotoğraf yoksa baş harfler gösterilir.
public struct LeaderAvatar: View {
    private let party: Party
    private let size: CGFloat

    public init(_ party: Party, size: CGFloat) {
        self.party = party
        self.size = size
    }

    public var body: some View {
        let images = (party.leaderPhotoAssets ?? []).prefix(2).compactMap { UIImage(named: $0) }
        HStack(spacing: -size * 0.32) {
            if images.isEmpty {
                circle {
                    Text(party.leaderInitials)
                        .font(.system(size: size * 0.38, weight: .semibold, design: .rounded))
                        .foregroundStyle(.white)
                }
            } else {
                ForEach(images.indices, id: \.self) { index in
                    circle {
                        Image(uiImage: images[index])
                            .resizable()
                            .scaledToFill()
                    }
                }
            }
        }
        .accessibilityElement()
        .accessibilityLabel(party.leaderName)
    }

    private func circle<Content: View>(@ViewBuilder _ content: () -> Content) -> some View {
        Circle()
            .fill(Color.white.opacity(0.14))
            .overlay { content() }
            .frame(width: size, height: size)
            .clipShape(Circle())
            .overlay { Circle().strokeBorder(Color.white.opacity(0.5), lineWidth: 1) }
    }
}
