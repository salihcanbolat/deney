import XCTest

/// Uygulamayı simülatörde açar ve ekran görüntüsü alır. CI bu test sırasında ekranı videoya kaydeder;
/// bekleme süreleri canlı önizlemenin bir turu videoya girsin diye var.
final class GalleryUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    func testGalleryOnSimulator() throws {
        let app = XCUIApplication()
        app.launchArguments += ["-AppleLanguages", "(tr)", "-AppleLocale", "tr_TR"]
        app.launch()

        XCTAssertTrue(app.staticTexts["Bileşenler"].waitForExistence(timeout: 20))
        try save("10-simulator-acilis")

        // Canlı önizleme: 1,4 sn dinlenme, sonra basılı tutma.
        Thread.sleep(forTimeInterval: 2.2)
        try save("11-simulator-basili")

        // Bir tur daha videoya girsin.
        Thread.sleep(forTimeInterval: 9)

        app.swipeUp()
        Thread.sleep(forTimeInterval: 1)
        try save("12-simulator-asagi")
    }

    private func save(_ name: String) throws {
        let png = XCUIScreen.main.screenshot().pngRepresentation
        let directory = URL(fileURLWithPath: #filePath)
            .deletingLastPathComponent()
            .deletingLastPathComponent()
            .deletingLastPathComponent()
            .appendingPathComponent("build/screenshots", isDirectory: true)
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        try png.write(to: directory.appendingPathComponent("\(name).png"))
        let attachment = XCTAttachment(data: png, uniformTypeIdentifier: "public.png")
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
