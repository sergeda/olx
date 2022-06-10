package utils

import services.getTime
import utils.AparserResponses.*
import zio.test.Assertion.*
import zio.test.*
import zio.test.{ZIOSpec, ZSpec, assertM, suite}
import utils.AparserUtils.*
import domain.Aparser.{Ad, AdDetail, *}

import java.time.LocalDateTime

object AparserUtilsSpec extends ZIOSpecDefault {

  override def spec = suite("AparserUtils")(
    test("testing parsing of Json for list of Ads") {
      val result = extractAparserResponse(jsonGetAdResponse).map(extractLines)
      val expect = Vector(
        Line(
          "https://www.olx.ua/d/obyavlenie/hostel-dlya-trivalogo-prozhivannya-1900-grn-tizhden-IDOjr4E.html#f263aebf2a;promoted,https://ireland.apollo.olxcdn.com:443/v1/files/7r3bg40r7mx23-UA/image;s=644x461,1900,\"грн\",\"Сегодня 20:18\",\"Львов, Железнодорожный\""
        ),
        Line(
          "https://www.olx.ua/d/obyavlenie/orenda-1-kmn-kv-ra-pr-svobodi-tsentr-msta-lvv-IDOjmy6.html#f263aebf2a;promoted,,23514,\"грн\",\"Сегодня 15:09\",\"Львов, Галицкий\""
        )
      )
      assertM(result)(equalTo(expect))
    },
    test("testing parsing of Json for getAdDetails") {
      val result = extractAparserResponse(jsonGetAdDetailsResp)
      val expect = AparserResponse(
        "\"Долгосрочная аренда квартир\",\"Вільна ! 3 кім квартира, Новобудова, Паркінг + Тераса. Ближній центр\",\"Василь\",2017-05-31T10:46:30+03:00,\"063 018 0101\",\"Львовская область, Львов, Галицкий\",1300,USD,175,2022-03-23T23:02:24+02:00,https://ireland.apollo.olxcdn.com:443/v1/files/vhfrkatojuer-UA/image;s=1280x960,728821139,https://www.olx.ua/d/obyavlenie/vlna-3-km-kvartira-novobudova-parkng-terasa-blizhny-tsentr-IDNk3FF.html#f263aebf2a\r\n"
      )
      assertM(result)(equalTo(expect))
    },
    test("createAd") {
      val result = createAd(
        Line(
          "https://www.olx.ua/d/obyavlenie/hostel-dlya-trivalogo-prozhivannya-1900-grn-tizhden-IDOjr4E.html#f263aebf2a;promoted,https://ireland.apollo.olxcdn.com:443/v1/files/7r3bg40r7mx23-UA/image;s=644x461,1900,\"грн\",\"Сегодня 20:18\",\"Львов, Железнодорожный\""
        )
      )
      val expect = Ad(
        AdUrl(
          "https://www.olx.ua/d/obyavlenie/hostel-dlya-trivalogo-prozhivannya-1900-grn-tizhden-IDOjr4E.html#f263aebf2a;promoted"
        ),
        AdId("IDOjr4E")
      )
      assertM(result)(equalTo(expect))
    },
    test("isPaid") {
      val result: Boolean = isPaid(
        AdUrl(
          "https://www.olx.ua/d/obyavlenie/hostel-dlya-trivalogo-prozhivannya-1900-grn-tizhden-IDOjr4E.html#f263aebf2a;promoted"
        )
      )
      val expect = true
      assertTrue(result == expect)
    },
    test("createAdDetail") {
      val result = createAdDetail(
        Line(
          "\"Долгосрочная аренда квартир\",\"Вільна ! 3 кім квартира Новобудова Паркінг + Тераса. Ближній центр\",\"Василь\",2017-05-31T10:46:30+03:00,\"063 018 0101\",\"Львовская область Львов Галицкий\",1300,USD,401,2022-03-23T23:02:24+02:00,https://ireland.apollo.olxcdn.com:443/v1/files/vhfrkatojuer-UA/image;s=1280x960,728821139,https://www.olx.ua/d/obyavlenie/vlna-3-km-kvartira-novobudova-parkng-terasa-blizhny-tsentr-IDNk3FF.html#f263aebf2a\r\n"
        )
      )
      val expect =
        AdDetail(
          adUrl = AdUrl(
            "https://www.olx.ua/d/obyavlenie/vlna-3-km-kvartira-novobudova-parkng-terasa-blizhny-tsentr-IDNk3FF.html#f263aebf2a"
          ),
          adId = AdId("IDNk3FF"),
          adPublished = AdPublished(getTime("2022-03-23T23:02:24+02:00").get),
          adOwnerRegistered =
            AdOwnerRegistered(getTime("2017-05-31T10:46:30+03:00").get),
          adOwnerPhone = AdOwnerPhone("063 018 0101"),
          adCost = AdCost("1300"),
          adCurrency = AdCurrency("USD"),
          adPhoto = AdPhoto(
            "https://ireland.apollo.olxcdn.com:443/v1/files/vhfrkatojuer-UA/image;s=1280x960"
          ),
          adOwner = AdOwner("Василь"),
          adLocation = AdLocation("Львовская область Львов Галицкий"),
          adTitle = AdTitle(
            "Вільна ! 3 кім квартира Новобудова Паркінг + Тераса. Ближній центр"
          )
        )
      assertM(result)(equalTo(expect))
    }
  )
}

object AparserResponses {

  val jsonGetAdDetailsResp =
    """
      |{
      |  "success": 1,
      |  "data": {
      |    "resultString": "\"Долгосрочная аренда квартир\",\"Вільна ! 3 кім квартира, Новобудова, Паркінг + Тераса. Ближній центр\",\"Василь\",2017-05-31T10:46:30+03:00,\"063 018 0101\",\"Львовская область, Львов, Галицкий\",1300,USD,175,2022-03-23T23:02:24+02:00,https://ireland.apollo.olxcdn.com:443/v1/files/vhfrkatojuer-UA/image;s=1280x960,728821139,https://www.olx.ua/d/obyavlenie/vlna-3-km-kvartira-novobudova-parkng-terasa-blizhny-tsentr-IDNk3FF.html#f263aebf2a\r\n",
      |    "logs": [
      |      [
      |        0,
      |        1648101743,
      |        "Parser JS::order::3218::0 parse query https://www.olx.ua/d/obyavlenie/vlna-3-km-kvartira-novobudova-parkng-terasa-blizhny-tsentr-IDNk3FF.html#f263aebf2a"
      |      ],
      |      [
      |        0,
      |        1648101743,
      |        "Use proxy http://192.157.55.48:65432"
      |      ],
      |      [
      |        0,
      |        1648101746,
      |        "GET(1): https://www.olx.ua/d/obyavlenie/vlna-3-km-kvartira-novobudova-parkng-terasa-blizhny-tsentr-IDNk3FF.html#f263aebf2a - 200 OK (78.96 KB)"
      |      ],
      |      [
      |        0,
      |        1648101746,
      |        "POST(1): https://www.olx.ua/api/open/oauth/token/ - 200 OK (0.15 KB)"
      |      ],
      |      [
      |        0,
      |        1648101747,
      |        "GET(1): https://www.olx.ua/api/v1/offers/728821139/phones/ - 200 OK (0.03 KB)"
      |      ],
      |      [
      |        0,
      |        1648101748,
      |        "POST(1): https://www.olx.ua/api/v1/offers/728821139/page-views/ - 200 OK (0.01 KB)"
      |      ],
      |      [
      |        3,
      |        1648101748,
      |        1
      |      ],
      |      [
      |        0,
      |        1648101748,
      |        "Thread complete work"
      |      ]
      |    ]
      |  }
      |}
      |""".stripMargin

  val jsonGetAdResponse =
    """
      |{
      |  "success": 1,
      |  "data": {
      |    "resultString": "https://www.olx.ua/d/obyavlenie/hostel-dlya-trivalogo-prozhivannya-1900-grn-tizhden-IDOjr4E.html#f263aebf2a;promoted,https://ireland.apollo.olxcdn.com:443/v1/files/7r3bg40r7mx23-UA/image;s=644x461,1900,\"грн\",\"Сегодня 20:18\",\"Львов, Железнодорожный\"\r\nhttps://www.olx.ua/d/obyavlenie/orenda-1-kmn-kv-ra-pr-svobodi-tsentr-msta-lvv-IDOjmy6.html#f263aebf2a;promoted,,23514,\"грн\",\"Сегодня 15:09\",\"Львов, Галицкий\"\r\n",
      |    "logs": [
      |      [
      |        0,
      |        1648062569,
      |        "Parser JS::order::3219::0 parse query https://www.olx.ua/nedvizhimost/kvartiry/dolgosrochnaya-arenda-kvartir/lv/?search%5Bfilter_enum_furnish%5D%5B0%5D=yes"
      |      ],
      |      [
      |        0,
      |        1648062569,
      |        "Wait for proxy"
      |      ],
      |      [
      |        0,
      |        1648062574,
      |        "Use proxy http://107.152.227.231:65432"
      |      ],
      |      [
      |        0,
      |        1648062584,
      |        "GET(1): https://www.olx.ua/nedvizhimost/kvartiry/dolgosrochnaya-arenda-kvartir/lv/?search%5Bfilter_enum_furnish%5D%5B0%5D=yes - 200 OK (39.34 KB)"
      |      ],
      |      [
      |        0,
      |        1648062584,
      |        "Found 44 ads"
      |      ],
      |      [
      |        3,
      |        1648062585,
      |        1
      |      ],
      |      [
      |        0,
      |        1648062585,
      |        "Thread complete work"
      |      ]
      |    ]
      |  }
      |}
      |""".stripMargin

}
