package com.example.data

/**
 * قائمة الوحدات والأشكال الصيدلانية والمستلزمات الطبية المعتمدة في نظام صيدلي
 */
object PharmacyUnits {
    // 1. أقراص (ينقسم إلى كبسول وأقراص عادية)
    const val TABLETS_REGULAR = "أقراص عادية"
    const val CAPSULES = "كبسولات"
    // 2. شراب
    const val SYRUP = "شراب"
    // 3. مراهم
    const val OINTMENT = "مراهم"
    // 4. كريمات
    const val CREAM = "كريمات"
    // 5. تحاميل
    const val SUPPOSITORIES = "تحاميل"
    // 6. أمبولات
    const val AMPOULES = "أمبولات"
    // 7. فيالة
    const val VIAL = "فيالة"
    // 8. لصقات
    const val PATCHES = "لصقات"
    // 9. مناديل
    const val WIPES = "مناديل"
    // 10. حفاضات
    const val DIAPERS = "حفاضات"
    // 11. سيرنج
    const val SYRINGES = "سيرنج"
    // 12. مغذيات
    const val IV_FLUIDS = "مغذيات"
    // 13. فراشات
    const val BUTTERFLY_NEEDLES = "فراشات"
    // 14. جل
    const val GEL = "جل"
    // 15. بودرة أطفال
    const val BABY_POWDER = "بودرة أطفال"
    // 16. محاليل
    const val SOLUTIONS = "محاليل"
    // 17. فورات
    const val EFFERVESCENT = "فورات"
    // 18. حليب
    const val MILK = "حليب"
    // 19. رضاعات
    const val BABY_BOTTLES = "رضاعات"
    // 20. حلمات
    const val NIPPLES = "حلمات"
    // 21. كذابات
    const val PACIFIERS = "كذابات"
    // 22. سكر محلي
    const val SWEETENER = "سكر محلي"
    // 23. موصل مغذية
    const val IV_CONNECTOR = "موصل مغذية"
    // 24. كمامات
    const val MASKS = "كمامات"
    // 25. زيوت
    const val OILS = "زيوت"
    // 26. صوابين
    const val SOAPS = "صوابين"
    // 27. مشارط طبية
    const val SCALPELS = "مشارط طبية"
    // 28. خيوط طبية
    const val MEDICAL_SUTURES = "خيوط طبية"
    // 29. إبر جراحية
    const val SURGICAL_NEEDLES = "إبر جراحية"
    // 30. أحزمة طبية
    const val MEDICAL_BELTS = "أحزمة طبية"
    // 31. رباط ضاغط
    const val ELASTIC_BANDAGE = "رباط ضاغط"
    // 32. شاش معقم وشاش عادي
    const val STERILE_GAUZE = "شاش معقم"
    const val REGULAR_GAUZE = "شاش عادي"
    // 33. مطهرات جراحية
    const val SURGICAL_ANTISEPTICS = "مطهرات جراحية"
    // 34. مسحات طبية
    const val MEDICAL_SWABS = "مسحات طبية"
    // 35. نيكلات يد ورجل
    const val SPLINTS_HAND_LEG = "نيكلات يد ورجل"
    // 36. بخاخات
    const val SPRAYS = "بخاخات"
    // 37. قطرات
    const val DROPS = "قطرات"

    val allUnits = listOf(
        TABLETS_REGULAR,
        CAPSULES,
        SYRUP,
        OINTMENT,
        CREAM,
        SUPPOSITORIES,
        AMPOULES,
        VIAL,
        PATCHES,
        WIPES,
        DIAPERS,
        SYRINGES,
        IV_FLUIDS,
        BUTTERFLY_NEEDLES,
        GEL,
        BABY_POWDER,
        SOLUTIONS,
        EFFERVESCENT,
        MILK,
        BABY_BOTTLES,
        NIPPLES,
        PACIFIERS,
        SWEETENER,
        IV_CONNECTOR,
        MASKS,
        OILS,
        SOAPS,
        SCALPELS,
        MEDICAL_SUTURES,
        SURGICAL_NEEDLES,
        MEDICAL_BELTS,
        ELASTIC_BANDAGE,
        STERILE_GAUZE,
        REGULAR_GAUZE,
        SURGICAL_ANTISEPTICS,
        MEDICAL_SWABS,
        SPLINTS_HAND_LEG,
        SPRAYS,
        DROPS
    )

    data class UnitCategory(
        val categoryName: String,
        val units: List<String>
    )

    val categorizedUnits = listOf(
        UnitCategory(
            categoryName = "أشكال دوائية",
            units = listOf(
                TABLETS_REGULAR,
                CAPSULES,
                SYRUP,
                EFFERVESCENT,
                DROPS,
                SPRAYS,
                SUPPOSITORIES,
                AMPOULES,
                VIAL,
                SOLUTIONS,
                OINTMENT,
                CREAM,
                GEL
            )
        ),
        UnitCategory(
            categoryName = "مستلزمات ومستهلكات",
            units = listOf(
                SYRINGES,
                IV_FLUIDS,
                IV_CONNECTOR,
                BUTTERFLY_NEEDLES,
                MASKS,
                MEDICAL_SWABS,
                SURGICAL_ANTISEPTICS,
                PATCHES,
                STERILE_GAUZE,
                REGULAR_GAUZE,
                ELASTIC_BANDAGE
            )
        ),
        UnitCategory(
            categoryName = "أدوات جراحية وجبائر",
            units = listOf(
                SCALPELS,
                MEDICAL_SUTURES,
                SURGICAL_NEEDLES,
                MEDICAL_BELTS,
                SPLINTS_HAND_LEG
            )
        ),
        UnitCategory(
            categoryName = "عناية وأطفال",
            units = listOf(
                DIAPERS,
                BABY_POWDER,
                MILK,
                BABY_BOTTLES,
                NIPPLES,
                PACIFIERS,
                WIPES,
                SOAPS,
                OILS,
                SWEETENER
            )
        )
    )
}
