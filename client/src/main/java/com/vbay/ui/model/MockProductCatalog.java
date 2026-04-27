package com.vbay.ui.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MockProductCatalog {
    public static final String ELECTRONICS_MACBOOKS = "electronics.macbooks";
    public static final String ELECTRONICS_PHONES = "electronics.phones";
    public static final String ELECTRONICS_GAMING = "electronics.gaming";
    public static final String ELECTRONICS_AUDIO = "electronics.audio";
    public static final String COLLECTIBLES_CARDS = "collectibles.cards";
    public static final String COLLECTIBLES_FIGURES = "collectibles.figures";
    public static final String ART_STAMPS = "art.stamps";
    public static final String ART_PAINTBRUSHES = "art.paintbrushes";
    public static final String JEWELRY_CLOCKS = "jewelry.clocks";
    public static final String JEWELRY_EARRINGS = "jewelry.earrings";
    public static final String SPORTING_RACKETS = "sporting.rackets";
    public static final String SPORTING_PICKLEBALL = "sporting.pickleball";

    private MockProductCatalog() {
    }

    public static Map<String, List<Product>> categoryData() {
        Map<String, List<Product>> data = new LinkedHashMap<>();

        data.put(ELECTRONICS_MACBOOKS, List.of(
            product(
                "Apple MacBook Pro 16 M3 Max",
                "Top-spec creator laptop with strong auction momentum and a premium finish.",
                "$3,450.00",
                "$2,900.00",
                "$125.00",
                "2h 45m left",
                0.85,
                "/jfx/image/products/macbookPro.png"
            ),
            product(
                "MacBook Air 15 Midnight",
                "Thin-and-light daily machine packaged as a sealed flagship bundle.",
                "$1,790.00",
                "$1,320.00",
                "$60.00",
                "5h 12m left",
                0.58,
                "/jfx/image/products/macbookPro.png"
            ),
            product(
                "MacBook Pro Studio Bundle",
                "Production-ready setup targeting editors who want one high-end lot.",
                "$2,980.00",
                "$2,350.00",
                "$90.00",
                "7h 03m left",
                0.41,
                "/jfx/image/products/macbookPro.png"
            )
        ));

        data.put(ELECTRONICS_PHONES, List.of(
            product(
                "iPhone 15 Pro Max 1TB Titanium",
                "Factory-unlocked flagship with strong demand and clean battery health.",
                "$1,680.00",
                "$1,240.00",
                "$55.00",
                "1h 18m left",
                0.64,
                "/jfx/image/products/iphone15.png"
            ),
            product(
                "Galaxy Fold Premiere Edition",
                "Large-display foldable listed for premium productivity bidders.",
                "$1,540.00",
                "$1,180.00",
                "$50.00",
                "3h 05m left",
                0.49,
                "/jfx/image/products/iphone15.png"
            ),
            product(
                "Pixel Pro Collector Set",
                "Minimalist Android flagship with sealed accessories and fast traction.",
                "$1,120.00",
                "$860.00",
                "$40.00",
                "6h 26m left",
                0.37,
                "/jfx/image/products/iphone15.png"
            )
        ));

        data.put(ELECTRONICS_GAMING, List.of(
            product(
                "RTX 4090 Liquid Cooled Build",
                "Showcase gaming tower built for high-refresh competitive play.",
                "$2,240.00",
                "$1,850.00",
                "$100.00",
                "3h 06m left",
                0.58,
                "/jfx/image/products/RTX4090.png"
            ),
            product(
                "ROG STRIX DDR5 Memory Lot",
                "Performance memory kit paired for modern enthusiast systems.",
                "$420.00",
                "$300.00",
                "$20.00",
                "8h 42m left",
                0.33,
                "/jfx/image/products/ram.png"
            ),
            product(
                "Compact Esports Desktop",
                "Small-form machine tuned for tournament travel and low-latency play.",
                "$1,390.00",
                "$1,020.00",
                "$45.00",
                "4h 51m left",
                0.62,
                "/jfx/image/products/RTX4090.png"
            )
        ));

        data.put(ELECTRONICS_AUDIO, List.of(
            product(
                "ASUS ROG Kithara Planar",
                "Closed-back planar headset tuned for immersive detail and streaming.",
                "$1,120.00",
                "$790.00",
                "$35.00",
                "5h 31m left",
                0.39,
                "/jfx/image/products/headphones.png"
            ),
            product(
                "Reference Studio Headphones",
                "Neutral tuning and clean staging for long editing sessions.",
                "$760.00",
                "$540.00",
                "$25.00",
                "2h 58m left",
                0.54,
                "/jfx/image/products/headphones.png"
            ),
            product(
                "Desktop DAC + Headphone Stack",
                "Compact listening chain designed for a premium desk audio setup.",
                "$935.00",
                "$660.00",
                "$30.00",
                "9h 11m left",
                0.28,
                "/jfx/image/products/headphones.png"
            )
        ));

        data.put(COLLECTIBLES_CARDS, List.of(
            product(
                "CR7 TOTY +8 Rare Card",
                "Highly chased football card with strong grading upside and clean centering.",
                "$4,250.00",
                "$3,300.00",
                "$150.00",
                "5h 02m left",
                0.66,
                "/jfx/image/products/cardCR7.png"
            ),
            product(
                "Hall of Fame Signature Insert",
                "Short-print memorabilia card positioned for long-term collectors.",
                "$2,180.00",
                "$1,620.00",
                "$80.00",
                "6h 49m left",
                0.44,
                "/jfx/image/products/cardCR7.png"
            ),
            product(
                "Sealed Premium Hobby Box",
                "Unopened collector box with chase potential and display value.",
                "$1,540.00",
                "$1,120.00",
                "$60.00",
                "4h 27m left",
                0.52,
                "/jfx/image/products/cardCR7.png"
            )
        ));

        data.put(COLLECTIBLES_FIGURES, List.of(
            product(
                "Hot Toys Iron Man Mark XLVI Diecast",
                "Articulated collector figure with premium finish and display presence.",
                "$760.00",
                "$520.00",
                "$25.00",
                "2h 21m left",
                0.48,
                "/jfx/image/products/Iron_Man_1.png"
            ),
            product(
                "Museum Pose Iron Man Variant",
                "Limited run display piece with magnetic accessories and clean paintwork.",
                "$980.00",
                "$720.00",
                "$35.00",
                "7h 32m left",
                0.31,
                "/jfx/image/products/Iron_Man_1.png"
            ),
            product(
                "Collector Armor Diorama Set",
                "Shelf-ready hero display with layered base and premium box art.",
                "$1,240.00",
                "$910.00",
                "$45.00",
                "8h 05m left",
                0.42,
                "/jfx/image/products/Iron_Man_1.png"
            )
        ));

        data.put(ART_STAMPS, List.of(
            product(
                "Historic Vietnam Presidential Stamp",
                "A high-interest philatelic piece framed around rarity and preservation.",
                "$1,905.00",
                "$1,300.00",
                "$55.00",
                "8h 14m left",
                0.34,
                "/jfx/image/products/stamps.png"
            ),
            product(
                "Indochina Archive Stamp Sheet",
                "Multi-stamp sheet for collectors building archival regional sets.",
                "$860.00",
                "$610.00",
                "$28.00",
                "3h 49m left",
                0.57,
                "/jfx/image/products/stamps.png"
            ),
            product(
                "Museum Sleeve Postal Pair",
                "Protected pair curated for display-focused historical collections.",
                "$630.00",
                "$420.00",
                "$18.00",
                "5h 44m left",
                0.46,
                "/jfx/image/products/stamps.png"
            )
        ));

        data.put(ART_PAINTBRUSHES, List.of(
            product(
                "Limited Nylon Wood Brush Kit 24pcs",
                "Complete brush set for studio painting and mixed-media sessions.",
                "$285.00",
                "$180.00",
                "$10.00",
                "1h 52m left",
                0.57,
                "/jfx/image/products/paintbrushes.png"
            ),
            product(
                "Master Atelier Brush Roll",
                "Portable artist roll balancing fine detail and wash coverage.",
                "$190.00",
                "$120.00",
                "$8.00",
                "4h 20m left",
                0.29,
                "/jfx/image/products/paintbrushes.png"
            ),
            product(
                "Collector Brush Display Set",
                "Presentation-grade brush collection for decorators and enthusiasts.",
                "$340.00",
                "$225.00",
                "$12.00",
                "6h 15m left",
                0.51,
                "/jfx/image/products/paintbrushes.png"
            )
        ));

        data.put(JEWELRY_CLOCKS, List.of(
            product(
                "Epos Emotion Moonphase Triple Calendar",
                "Complication-heavy timepiece with strong collector storytelling.",
                "$3,180.00",
                "$2,420.00",
                "$110.00",
                "9h 03m left",
                0.29,
                "/jfx/image/products/clocks.png"
            ),
            product(
                "Blue Velvet Omega Desk Set",
                "A dramatic display clock piece presented as a luxury desk focal point.",
                "$5,480.00",
                "$4,250.00",
                "$180.00",
                "7h 21m left",
                0.43,
                "/jfx/image/products/clocks.png"
            ),
            product(
                "Heritage Brass Mantel Clock",
                "Warm metallic finish aimed at buyers building refined interiors.",
                "$1,960.00",
                "$1,440.00",
                "$70.00",
                "5h 36m left",
                0.55,
                "/jfx/image/products/clocks.png"
            )
        ));

        data.put(JEWELRY_EARRINGS, List.of(
            product(
                "18K Diamond Halo Drop Earrings",
                "Formal statement pair with bright stones and balanced proportions.",
                "$2,140.00",
                "$1,580.00",
                "$75.00",
                "3h 11m left",
                0.55,
                "/jfx/image/products/earrings.png"
            ),
            product(
                "Velvet Sapphire Evening Pair",
                "Deep blue accent stones set for high-contrast formal styling.",
                "$2,920.00",
                "$2,180.00",
                "$95.00",
                "4h 42m left",
                0.38,
                "/jfx/image/products/neckles.png"
            ),
            product(
                "Pearl Cascade Signature Drops",
                "Elegant collector jewelry designed for gala and ceremony use.",
                "$1,480.00",
                "$1,020.00",
                "$50.00",
                "6h 09m left",
                0.47,
                "/jfx/image/products/earrings.png"
            )
        ));

        data.put(SPORTING_RACKETS, List.of(
            product(
                "Astrox 100ZZ Kurenai Limited 2024",
                "Explosive premium badminton racket listed for competitive players.",
                "$410.00",
                "$280.00",
                "$15.00",
                "2h 08m left",
                0.44,
                "/jfx/image/products/astrox100zz_kurenai.png"
            ),
            product(
                "Tournament Frame Twin Pack",
                "Two-racket bundle targeting club players upgrading together.",
                "$680.00",
                "$470.00",
                "$25.00",
                "5h 54m left",
                0.36,
                "/jfx/image/products/astrox100zz_kurenai.png"
            ),
            product(
                "Signed Match-Day Racket",
                "Display-worthy performance frame with collector crossover appeal.",
                "$1,160.00",
                "$790.00",
                "$40.00",
                "8h 12m left",
                0.25,
                "/jfx/image/products/astrox100zz_kurenai.png"
            )
        ));

        data.put(SPORTING_PICKLEBALL, List.of(
            product(
                "Set of 5 Carbon-Optimized Paddles",
                "Complete paddle bundle with a balanced mix of control and pop.",
                "$2,350.00",
                "$1,860.00",
                "$85.00",
                "1h 29m left",
                0.73,
                "/jfx/image/products/paddles.png"
            ),
            product(
                "Elite Control Paddle Pair",
                "Two premium faces tuned for faster kitchen exchanges and resets.",
                "$920.00",
                "$680.00",
                "$30.00",
                "4h 38m left",
                0.45,
                "/jfx/image/products/paddles.png"
            ),
            product(
                "Recovery Spray + Paddle Bundle",
                "Hybrid sport lot built for club players buying match-day essentials.",
                "$180.00",
                "$110.00",
                "$8.00",
                "3h 17m left",
                0.61,
                "/jfx/image/products/coldspray.png"
            )
        ));

        return Map.copyOf(data);
    }

    private static Product product(
        String title,
        String description,
        String price,
        String startingPrice,
        String bidStep,
        String timeLeft,
        double progress,
        String imagePath
    ) {
        return new Product(title, description, price, startingPrice, bidStep, timeLeft, progress, imagePath);
    }
}
