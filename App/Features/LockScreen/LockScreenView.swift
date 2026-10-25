import SwiftUI
import UIKit
import DesignSystem

/// Uygulama içindeki kilit ekranı replikası (Ekran 1–3). Tasarımın birebir aynısı;
/// basılı tut + kaydır deneyimi burada yaşanır (Bölüm 3, seçenek A).
struct LockScreenView: View {
    let data: StripData
    var fixedDate: Date?

    @State private var held: Bool
    @State private var activeIndex: Int

    init(data: StripData, held: Bool = false, activeIndex: Int = 0, fixedDate: Date? = nil) {
        self.data = data
        self.fixedDate = fixedDate
        _held = State(initialValue: held)
        _activeIndex = State(initialValue: activeIndex)
    }

    var body: some View {
        ZStack(alignment: .top) {
            LockWallpaper()

            VStack(spacing: 0) {
                LockClock(fixedDate: fixedDate)
                StripView(data: data, held: $held, activeIndex: $activeIndex)
                    .padding(.top, StripMetrics.topSpacingBelowClock)
                Text("Sol mum parti, sağ mum lider · Basılı tut")
                    .font(LockFont.hint)
                    .foregroundStyle(Color.white.opacity(0.62))
                    .padding(.top, 10)
                    .opacity(held ? 0 : 1)
                    .animation(Motion.fade, value: held)
                    .accessibilityHidden(held)
            }
            .padding(.top, 66)

            LockQuickActions()
        }
        .ignoresSafeArea()
    }
}

/// Duvar kâğıdı: mürekkep tonlarında derin bir degrade, tasarımdaki büyük daire (kenarı ışık
/// yakalar), saatin arkasında hafif bir ışık ve ince bir film greni.
struct LockWallpaper: View {
    var body: some View {
        GeometryReader { proxy in
            ZStack {
                base
                Circle()
                    .fill(RadialGradient(
                        colors: [Color(hex: "#1E2740"), Color(hex: "#161D2E")],
                        center: .top, startRadius: 0, endRadius: 560
                    ))
                    .overlay {
                        Circle().strokeBorder(
                            LinearGradient(colors: [Color.white.opacity(0.09), .clear], startPoint: .top, endPoint: .center),
                            lineWidth: 1
                        )
                    }
                    .frame(width: 790, height: 790)
                    .position(x: proxy.size.width / 2, y: 300 + 395)
                RadialGradient(
                    colors: [Color(hex: "#5A4FC4").opacity(0.22), .clear],
                    center: UnitPoint(x: 0.5, y: 0.13), startRadius: 0, endRadius: 300
                )
                FilmGrain()
            }
            .frame(width: proxy.size.width, height: proxy.size.height)
            .clipped()
        }
        .accessibilityHidden(true)
    }

    @ViewBuilder
    private var base: some View {
        if #available(iOS 18.0, *) {
            MeshGradient(
                width: 3, height: 3,
                points: [
                    [0, 0], [0.5, 0], [1, 0],
                    [0, 0.45], [0.6, 0.4], [1, 0.5],
                    [0, 1], [0.45, 1], [1, 1],
                ],
                colors: [
                    Color(hex: "#151B2C"), Color(hex: "#1C1B40"), Color(hex: "#131A2A"),
                    Color(hex: "#141B2B"), Color(hex: "#181E31"), Color(hex: "#1B1937"),
                    Color(hex: "#0D111B"), Color(hex: "#10151F"), Color(hex: "#0E121D"),
                ]
            )
        } else {
            LinearGradient(colors: [Color(hex: "#181D33"), Palette.lockWallpaper, Color(hex: "#0E121C")], startPoint: .top, endPoint: .bottom)
        }
    }
}

/// Düz renkli yüzeylerin "dijital" görünmesini engelleyen ince gren. Bir kez üretilip döşenir.
struct FilmGrain: View {
    var body: some View {
        Image(uiImage: Self.tile)
            .resizable(resizingMode: .tile)
            .blendMode(.overlay)
            .opacity(0.5)
            .allowsHitTesting(false)
    }

    private static let tile: UIImage = {
        let side = 128
        let renderer = UIGraphicsImageRenderer(size: CGSize(width: side, height: side))
        return renderer.image { context in
            var seed: UInt64 = 0x5EC1_A0E7
            for y in 0..<side {
                for x in 0..<side {
                    seed = seed &* 6364136223846793005 &+ 1442695040888963407
                    let value = CGFloat((seed >> 33) % 1000) / 1000
                    UIColor(white: value, alpha: 0.10).setFill()
                    context.fill(CGRect(x: x, y: y, width: 1, height: 1))
                }
            }
        }
    }()
}

/// Tarih ve saat. Varsayılan olarak gerçek saati gösterir; ekran görüntüleri için sabitlenebilir.
struct LockClock: View {
    var fixedDate: Date?

    var body: some View {
        TimelineView(.everyMinute) { context in
            let date = fixedDate ?? context.date
            VStack(spacing: 0) {
                Text(Self.day.string(from: date))
                    .font(LockFont.date)
                    .tracking(0.1)
                    .foregroundStyle(Color.white.opacity(0.82))
                Text(Self.time.string(from: date))
                    .font(LockFont.clock)
                    .tracking(LockFont.clockTracking)
                    .monospacedDigit()
                    .foregroundStyle(LinearGradient(
                        colors: [Color.white.opacity(0.97), Color.white.opacity(0.86)],
                        startPoint: .top, endPoint: .bottom
                    ))
                    .shadow(color: .black.opacity(0.28), radius: 14, y: 6)
                    .contentTransition(.numericText())
                    .frame(height: 94)
            }
            .accessibilityElement(children: .combine)
        }
    }

    private static let day: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "tr_TR")
        formatter.dateFormat = "EEEE d MMMM"
        return formatter
    }()

    private static let time: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "tr_TR")
        formatter.dateFormat = "H:mm"
        return formatter
    }()
}

/// Alt köşelerdeki el feneri ve kamera düğmeleri (görsel; kilit ekranı hissi için).
struct LockQuickActions: View {
    var body: some View {
        VStack {
            Spacer()
            HStack {
                button("flashlight.off.fill")
                Spacer()
                button("camera.fill")
            }
            .padding(.horizontal, 46)
            .padding(.bottom, 46)
        }
        .accessibilityHidden(true)
    }

    private func button(_ symbol: String) -> some View {
        Image(systemName: symbol)
            .font(.system(size: 20, weight: .regular))
            .foregroundStyle(.white)
            .frame(width: 50, height: 50)
            .background {
                Circle()
                    .fill(.ultraThinMaterial)
                    .environment(\.colorScheme, .dark)
                    .overlay { Circle().fill(Color.white.opacity(0.08)) }
            }
    }
}

#Preview("Dinlenme") {
    LockScreenView(data: StripData(.standard))
}

#Preview("Basılı · BHP") {
    LockScreenView(data: StripData(.standard), held: true, activeIndex: 2)
}
