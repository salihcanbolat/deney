import SwiftUI
import XCTest
@testable import SecimAnketi

/// Galeri bölümlerini PNG olarak `build/screenshots/` altına yazar. CI bu klasörü
/// `docs/screenshots/latest/` altına kopyalar.
@MainActor
final class GallerySnapshotTests: XCTestCase {
    func testRenderGallery() throws {
        let sample = StripSample(.standard)
        try render(GalleryContent(animated: false).padding(.vertical, 16), name: "01-galeri-tam", scale: 2)
        try render(section("MUM ÇİFTİ") { CandleStatesCard(sample: sample) }, name: "02-mum-cifti")
        try render(section("PARTİ LOGOSU") { LogoShowcase(parties: sample.columns.map(\.party)) }, name: "03-parti-logosu")
        try render(section("RENKLER") { TokenList() }, name: "04-renkler")
        try render(section("KOYU MOD ÖNERİSİ") { DarkModeProposal() }, name: "05-koyu-mod-onerisi")
        try render(section("YAZI") { TypeRamp() }, name: "06-yazi")
    }

    private func section<Content: View>(_ title: String, @ViewBuilder content: () -> Content) -> some View {
        GroupedSection(title: title, content: content)
            .padding(16)
    }

    private func render<V: View>(_ view: V, name: String, width: CGFloat = 390, scale: CGFloat = 3) throws {
        let content = view
            .frame(width: width)
            .background(Color(uiColor: .systemGroupedBackground))
            .environment(\.colorScheme, .light)
        let renderer = ImageRenderer(content: content)
        renderer.scale = scale
        renderer.proposedSize = ProposedViewSize(width: width, height: nil)
        let image = try XCTUnwrap(renderer.uiImage, "\(name) çizilemedi")
        try ScreenshotStore.save(XCTUnwrap(image.pngData()), name: name, in: self)
    }
}

enum ScreenshotStore {
    /// Depo kökündeki `build/screenshots/`. Simülatör testleri Mac'in dosya sistemine yazabilir.
    static let directory = URL(fileURLWithPath: #filePath)
        .deletingLastPathComponent()
        .deletingLastPathComponent()
        .deletingLastPathComponent()
        .appendingPathComponent("build/screenshots", isDirectory: true)

    static func save(_ png: Data, name: String, in testCase: XCTestCase) throws {
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        try png.write(to: directory.appendingPathComponent("\(name).png"))
        let attachment = XCTAttachment(data: png, uniformTypeIdentifier: "public.png")
        attachment.name = name
        attachment.lifetime = .keepAlways
        testCase.add(attachment)
    }
}
