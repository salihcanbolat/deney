import XCTest
@testable import PollCore

final class MockDatasetTests: XCTestCase {
    let data = MockDataset.standard

    func testCounts() {
        XCTAssertEqual(data.parties.count, 17)
        XCTAssertEqual(Set(data.parties.map(\.id)).count, data.parties.count)
        XCTAssertEqual(Set(data.parties.map(\.code)).count, data.parties.count)
        XCTAssertEqual(data.polls.map(\.title), ["Genel Seçim Anketi", "Genç Seçmen Anketi", "Lider Tercihi Anketi", "Asla Oy Vermem Anketi"])
        for poll in data.polls {
            XCTAssertEqual(data.result(for: poll.id)?.rows.count, data.parties.count, poll.id)
        }
    }

    /// Oranlar uydurulmaz: tüm sonuçlar sembolik ve her parti için eşit.
    func testResultsAreSymbolicAndEqual() {
        for result in data.results {
            XCTAssertTrue(result.isSymbolic, result.pollId)
            XCTAssertEqual(Set(result.rows.flatMap(\.allValues)).count, 1, result.pollId)
        }
        XCTAssertTrue(data.polls.allSatisfy { $0.participantCount == 0 && !$0.isLive })
    }

    /// Eşit değerlerde sıra, verideki sıradır: TBMM sandalye sayısı.
    func testOrderFollowsSeatCount() throws {
        let general = try XCTUnwrap(data.result(for: "genel-secim"))
        let seats = general.rows.compactMap { data.party($0.partyId)?.seats }
        XCTAssertEqual(seats, seats.sorted(by: >))
        XCTAssertEqual(general.top(8).compactMap { data.party($0.partyId)?.code },
                       ["AK PARTİ", "YENİ PARTİ", "DEM PARTİ", "MHP", "CHP", "İYİ PARTİ", "YENİ YOL", "HÜDA PAR"])
        XCTAssertEqual(general.place(of: "mhp"), 4)
    }

    func testPollTypesMatchCollectedSections() throws {
        let leaderPoll = try XCTUnwrap(data.result(for: "lider-tercihi"))
        XCTAssertTrue(leaderPoll.rows.allSatisfy { $0.partyNeg == nil && $0.leaderNeg == nil && $0.partyPos != nil })
        let neverPoll = try XCTUnwrap(data.result(for: "asla-oy-vermem"))
        XCTAssertTrue(neverPoll.rows.allSatisfy { $0.partyPos == nil && $0.leaderPos == nil && $0.partyNeg != nil })
        XCTAssertEqual(data.poll("asla-oy-vermem")?.type, .never)
        XCTAssertEqual(data.poll("lider-tercihi")?.type, .support)
    }

    func testNoSampleBallots() {
        XCTAssertTrue(data.sampleUser.ballots.isEmpty)
        XCTAssertEqual(data.sampleUser.prefs.lockScreenPollId, "genel-secim")
    }

    func testRepositoryThrowsForUnknownPoll() async {
        let repository = MockPollRepository()
        do {
            _ = try await repository.result(for: "yok")
            XCTFail("hata bekleniyordu")
        } catch {
            XCTAssertEqual(error as? PollRepositoryError, .unknownPoll("yok"))
        }
    }
}

final class CandleScaleTests: XCTestCase {
    func testCeilingRoundsPeakUp() {
        let rows = [PollResultRow(partyId: "a", partyPos: 16.4, leaderNeg: 26.9), PollResultRow(partyId: "b", partyPos: 3)]
        XCTAssertEqual(CandleScale(rows: rows).ceiling, 27)
    }

    /// LockRest.dc.html: `Math.max(2, v / MAX * (zone - 2))`
    func testHeightFormula() {
        let scale = CandleScale(ceiling: 27)
        XCTAssertEqual(scale.height(for: 16.4, in: 34), 16.4 / 27 * 32, accuracy: 0.0001)
        XCTAssertEqual(scale.height(for: 26.9, in: 70), 26.9 / 27 * 68, accuracy: 0.0001)
        XCTAssertEqual(scale.height(for: 0.1, in: 92), 2)
        XCTAssertEqual(scale.height(for: 27, in: 92), 90)
        XCTAssertEqual(scale.height(for: nil, in: 92), 0)
        XCTAssertEqual(scale.height(for: 12, in: 0), 0)
    }

    func testCeilingNeverBelowOne() {
        XCTAssertEqual(CandleScale(ceiling: 0).ceiling, 1)
        XCTAssertEqual(CandleScale(rows: [PollResultRow]()).ceiling, 1)
    }
}

final class StripGeometryTests: XCTestCase {
    func testColumnFromX() {
        let width = 334.0  // 350 pt şerit − 2 × 8 pt iç boşluk
        XCTAssertEqual(StripGeometry.column(at: 0, width: width), 0)
        XCTAssertEqual(StripGeometry.column(at: width / 8 - 0.01, width: width), 0)
        XCTAssertEqual(StripGeometry.column(at: width / 8, width: width), 1)
        XCTAssertEqual(StripGeometry.column(at: width / 2, width: width), 4)
        XCTAssertEqual(StripGeometry.column(at: width - 0.01, width: width), 7)
    }

    func testClampsOutsideTheStrip() {
        XCTAssertEqual(StripGeometry.column(at: -40, width: 334), 0)
        XCTAssertEqual(StripGeometry.column(at: 334, width: 334), 7)
        XCTAssertEqual(StripGeometry.column(at: 900, width: 334), 7)
        XCTAssertEqual(StripGeometry.column(at: .nan, width: 334), 0)
        XCTAssertEqual(StripGeometry.column(at: 10, width: 0), 0)
    }
}

final class FormattingTests: XCTestCase {
    func testPercent() {
        XCTAssertEqual(TurkishFormat.percent(16.4), "%16,4")
        XCTAssertEqual(TurkishFormat.percent(3), "%3,0")
        XCTAssertEqual(TurkishFormat.percent(26.94), "%26,9")
    }

    func testVotes() {
        XCTAssertEqual(TurkishFormat.votes(128_460), "128.460 oy")
        XCTAssertEqual(TurkishFormat.votes(23_118), "23.118 oy")
        XCTAssertEqual(TurkishFormat.grouped(999), "999")
        XCTAssertEqual(TurkishFormat.grouped(1_000), "1.000")
        XCTAssertEqual(TurkishFormat.grouped(1_234_567), "1.234.567")
        XCTAssertEqual(TurkishFormat.grouped(0), "0")
    }

    func testMonogramAndInitials() {
        let party = Party(id: "x", code: "ÇEP", name: "Çevre ve Emek Partisi", leaderName: "Deniz Kaya", color: "#3B7F2A")
        XCTAssertEqual(party.monogram, "Ç")
        XCTAssertEqual(party.leaderInitials, "DK")
        let single = Party(id: "y", code: "İDP", name: "İklim ve Doğa Partisi", leaderName: "Ufuk", color: "#2F6B4F")
        XCTAssertEqual(single.monogram, "İ")
        XCTAssertEqual(single.leaderInitials, "U")
    }
}
