import XCTest

/// Kilit ekranı replikası: tasarımdaki üç durum (LockRest, LockPress, LockDrag) ve gerçek
/// basılı tut + kaydır jesti. CI bu testler sırasında ekranı videoya kaydeder.
final class LockScreenUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    func testDesignStates() throws {
        try launch().saveScreenshot("20-kilit-dinlenme")
        try launch(["-stripHeld", "YES", "-stripIndex", "0"]).saveScreenshot("21-kilit-basili-akp")
        try launch(["-stripHeld", "YES", "-stripIndex", "2"]).saveScreenshot("22-kilit-kaydirma-dem")
    }

    func testPressAndDragAcrossParties() throws {
        let app = launch()
        let first = column(app, named: "Adalet ve Kalkınma Partisi")
        let sixth = column(app, named: "İYİ Parti")
        let third = column(app, named: "Halkların Eşitlik ve Demokrasi Partisi")
        XCTAssertTrue(first.waitForExistence(timeout: 10))

        // Basılı tut, parmağı kaldırmadan sağa kaydır, bekle.
        first.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
            .press(forDuration: 0.6, thenDragTo: sixth.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)),
                   withVelocity: .slow, thenHoldForDuration: 1.2)
        Thread.sleep(forTimeInterval: 1)

        // İkinci tur: üçüncü partide bırak.
        sixth.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
            .press(forDuration: 0.6, thenDragTo: third.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)),
                   withVelocity: .slow, thenHoldForDuration: 1.2)
        Thread.sleep(forTimeInterval: 1)

        // Bırakınca dinlenme hâline döner: yardım metni yeniden görünür.
        XCTAssertTrue(app.staticTexts["Sol mum parti, sağ mum lider · Basılı tut"].waitForExistence(timeout: 3))
    }

    // MARK: Yardımcılar

    private func launch(_ arguments: [String] = []) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += ["-AppleLanguages", "(tr)", "-AppleLocale", "tr_TR", "-demoDate", "YES"] + arguments
        app.launch()
        let clock = app.descendants(matching: .any).matching(NSPredicate(format: "label CONTAINS %@", "9:41")).firstMatch
        XCTAssertTrue(clock.waitForExistence(timeout: 20))
        Thread.sleep(forTimeInterval: 1.2)
        return app
    }

    private func column(_ app: XCUIApplication, named party: String) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label BEGINSWITH %@", party + ":"))
            .firstMatch
    }
}

private extension XCUIApplication {
    func saveScreenshot(_ name: String) throws {
        let png = XCUIScreen.main.screenshot().pngRepresentation
        let directory = URL(fileURLWithPath: #filePath)
            .deletingLastPathComponent()
            .deletingLastPathComponent()
            .deletingLastPathComponent()
            .appendingPathComponent("build/screenshots", isDirectory: true)
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        try png.write(to: directory.appendingPathComponent("\(name).png"))
    }
}
