// swift-tools-version: 5.9
import PackageDescription

// Tasarım token'ları ve ortak bileşenler (PartyLogo, VoteCandlePair).
// Uygulama ve widget eklentisi aynı bileşenleri kullanır.
let package = Package(
    name: "DesignSystem",
    platforms: [.iOS(.v17)],
    products: [
        .library(name: "DesignSystem", targets: ["DesignSystem"]),
    ],
    dependencies: [
        .package(path: "../PollCore"),
    ],
    targets: [
        .target(name: "DesignSystem", dependencies: [.product(name: "PollCore", package: "PollCore")]),
    ]
)
