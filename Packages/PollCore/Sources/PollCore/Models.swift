import Foundation

// Veri modeli: CLAUDE.md Bölüm 10. Tüm partiler, liderler ve rakamlar uydurmadır.

public struct Party: Identifiable, Hashable, Codable, Sendable {
    public let id: String
    public let code: String
    public let name: String
    public let leaderName: String
    /// Logo yer tutucusunun rengi ("#RRGGBB"). Yalnızca logolarda kullanılır, mumlarda asla.
    public let color: String
    /// Gerçek logo geldiğinde asset adı. Yoksa parti renginde daire + harf çizilir.
    public let logoAsset: String?
    /// Logonun kendi zemini var (kare ya da tam daire): rozeti doldurur. Yoksa beyaz rozetin içine oturur.
    public let logoFills: Bool?
    /// Lider fotoğrafları için asset adları (eş genel başkanlı partilerde iki tane). Yoksa baş harfler gösterilir.
    public let leaderPhotoAssets: [String]?
    /// TBMM'deki sandalye sayısı (sıralama için; oy oranı değildir).
    public let seats: Int?

    public init(id: String, code: String, name: String, leaderName: String, color: String, logoAsset: String? = nil, logoFills: Bool? = nil, leaderPhotoAssets: [String]? = nil, seats: Int? = nil) {
        self.id = id
        self.code = code
        self.name = name
        self.leaderName = leaderName
        self.color = color
        self.logoAsset = logoAsset
        self.logoFills = logoFills
        self.leaderPhotoAssets = leaderPhotoAssets
        self.seats = seats
    }

    /// Logo yer tutucusundaki harf: kodun ilk harfi ("ÇEP" → "Ç").
    public var monogram: String { String(code.prefix(1)) }

    /// Lider fotoğrafı yer tutucusu için baş harfler ("Murat Ergin" → "ME").
    public var leaderInitials: String {
        let words = leaderName.split(separator: " ")
        guard let first = words.first?.first else { return "" }
        guard words.count > 1, let last = words.last?.first else { return String(first) }
        return String(first) + String(last)
    }
}

public enum PollType: String, Codable, Sendable, CaseIterable {
    case support
    case never
    case supportAndNever

    public var includesSupport: Bool { self != .never }
    public var includesNever: Bool { self != .support }

    public var displayName: String {
        switch self {
        case .support: "Yalnız destek"
        case .never: "Yalnız asla"
        case .supportAndNever: "Destek + Asla"
        }
    }
}

public struct Poll: Identifiable, Hashable, Codable, Sendable {
    public let id: String
    public let title: String
    public let type: PollType
    public let includesLeader: Bool
    public let participantCount: Int
    public let isLive: Bool
    public let partyIds: [Party.ID]

    public init(id: String, title: String, type: PollType, includesLeader: Bool, participantCount: Int, isLive: Bool, partyIds: [Party.ID]) {
        self.id = id
        self.title = title
        self.type = type
        self.includesLeader = includesLeader
        self.participantCount = participantCount
        self.isLive = isLive
        self.partyIds = partyIds
    }
}

/// Bir partinin bir anketteki sonucu. Değerler yüzdedir (0–100).
/// Anket o bölümü toplamıyorsa değer `nil` olur (ör. "Yalnız destek" anketinde eksi oylar).
public struct PollResultRow: Hashable, Codable, Sendable {
    public let partyId: Party.ID
    public let partyPos: Double?
    public let leaderPos: Double?
    public let partyNeg: Double?
    public let leaderNeg: Double?
    public let partyPosCount: Int?
    public let leaderPosCount: Int?
    public let partyNegCount: Int?
    public let leaderNegCount: Int?

    public init(
        partyId: Party.ID,
        partyPos: Double? = nil, leaderPos: Double? = nil, partyNeg: Double? = nil, leaderNeg: Double? = nil,
        partyPosCount: Int? = nil, leaderPosCount: Int? = nil, partyNegCount: Int? = nil, leaderNegCount: Int? = nil
    ) {
        self.partyId = partyId
        self.partyPos = partyPos
        self.leaderPos = leaderPos
        self.partyNeg = partyNeg
        self.leaderNeg = leaderNeg
        self.partyPosCount = partyPosCount
        self.leaderPosCount = leaderPosCount
        self.partyNegCount = partyNegCount
        self.leaderNegCount = leaderNegCount
    }

    /// Sıralama değeri: olumlu parti oyu; anket yalnızca "asla" topluyorsa olumsuz parti oyu.
    public var rankingValue: Double { partyPos ?? partyNeg ?? 0 }

    /// Bu satırdaki tüm değerler (ortak mum ölçeğini hesaplamak için).
    public var allValues: [Double] { [partyPos, leaderPos, partyNeg, leaderNeg].compactMap { $0 } }
}

public struct PollResult: Hashable, Codable, Sendable {
    public let pollId: Poll.ID
    public let updatedAt: Date
    /// Sıralama değerine göre azalan sırada; eşit değerlerde verideki sıra korunur.
    public let rows: [PollResultRow]
    /// Değerler gerçek oy değil, yalnızca görünümü göstermek için konmuş eşit, sembolik değerler.
    /// Arayüz bu durumda oran yazmaz ve "Sembolik" etiketi gösterir.
    public let isSymbolic: Bool

    public init(pollId: Poll.ID, updatedAt: Date, rows: [PollResultRow], isSymbolic: Bool = false) {
        self.pollId = pollId
        self.updatedAt = updatedAt
        self.isSymbolic = isSymbolic
        self.rows = rows.enumerated()
            .sorted { lhs, rhs in
                lhs.element.rankingValue != rhs.element.rankingValue
                    ? lhs.element.rankingValue > rhs.element.rankingValue
                    : lhs.offset < rhs.offset
            }
            .map(\.element)
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.init(
            pollId: try container.decode(Poll.ID.self, forKey: .pollId),
            updatedAt: try container.decode(Date.self, forKey: .updatedAt),
            rows: try container.decode([PollResultRow].self, forKey: .rows),
            isSymbolic: try container.decodeIfPresent(Bool.self, forKey: .isSymbolic) ?? false
        )
    }

    /// Kilit ekranı şeridinde gösterilen ilk `count` parti (tasarımda 8).
    public func top(_ count: Int) -> [PollResultRow] { Array(rows.prefix(count)) }

    /// Partinin sırası, 1'den başlar ("3. sıra").
    public func place(of partyId: Party.ID) -> Int? {
        rows.firstIndex { $0.partyId == partyId }.map { $0 + 1 }
    }
}

/// Kullanıcının bir ankette bastığı mühürler. Yalnızca cihazda saklanır.
public struct Ballot: Hashable, Codable, Sendable {
    public let pollId: Poll.ID
    public var partyYes: Party.ID?
    /// Lider seçimi, liderin partisinin kimliğiyle tutulur.
    public var leaderYes: Party.ID?
    public var never: Party.ID?
    public var castAt: Date?

    public init(pollId: Poll.ID, partyYes: Party.ID? = nil, leaderYes: Party.ID? = nil, never: Party.ID? = nil, castAt: Date? = nil) {
        self.pollId = pollId
        self.partyYes = partyYes
        self.leaderYes = leaderYes
        self.never = never
        self.castAt = castAt
    }

    /// Basılan mühür sayısı ("2 mühür").
    public var stampCount: Int { [partyYes, leaderYes, never].compactMap { $0 }.count }
}

public struct UserPrefs: Hashable, Codable, Sendable {
    public var followedPollIds: Set<Poll.ID>
    public var lockScreenPollId: Poll.ID?
    public var notificationsByPoll: [Poll.ID: Bool]

    public init(followedPollIds: Set<Poll.ID> = [], lockScreenPollId: Poll.ID? = nil, notificationsByPoll: [Poll.ID: Bool] = [:]) {
        self.followedPollIds = followedPollIds
        self.lockScreenPollId = lockScreenPollId
        self.notificationsByPoll = notificationsByPoll
    }
}
