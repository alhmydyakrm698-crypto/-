package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        PharmacyUser::class,
        SaleInvoice::class,
        SaleItem::class,
        DrugInteractionEntity::class,
        MedicineDosageEntity::class,
        CustomerEntity::class,
        CustomerLedgerEntity::class,
        RefillReminderEntity::class,
        MedicineCategoryEntity::class,
        SaleReturn::class
    ],
    version = 5,
    exportSchema = false
)
abstract class PharmacyDatabase : RoomDatabase() {
    abstract fun pharmacyDao(): PharmacyDao

    companion object {
        @Volatile
        private var INSTANCE: PharmacyDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PharmacyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmacyDatabase::class.java,
                    "saydali_pharmacy_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PharmacyDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(context: Context): PharmacyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharmacyDatabase::class.java,
                    "saydali_pharmacy_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PharmacyDatabaseCallback(CoroutineScope(Dispatchers.IO)))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PharmacyDatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.pharmacyDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: PharmacyDao) {
            // 1. Initial Products with Active Ingredients and Alternatives
            val initialProducts = listOf(
                Product(
                    name = "باراسيتامول 500 ملغ",
                    manufacturer = "الدوائية (SPIMACO)",
                    activeIngredient = "Paracetamol",
                    dosageForm = "أقراص عادية",
                    strength = "500 mg",
                    unit = "علبة",
                    expiryDate = "2026-11-20",
                    unitPrice = 2.00,
                    wholesalePrice = 1.30,
                    barcode = "628100100201",
                    quantity = 0, // رصيد صفر لتجربة محرك البدائل التلقائي!
                    minStockLevel = 5,
                    category = "مسكنات وخافضات حرارة",
                    preferredSupplier = "شركة الدوائية الوطنية",
                    dailyBurnRate = 3.5
                ),
                Product(
                    name = "فيفادول 500 ملغ",
                    manufacturer = "SPIMACO السعودية",
                    activeIngredient = "Paracetamol",
                    dosageForm = "أقراص عادية",
                    strength = "500 mg",
                    unit = "علبة",
                    expiryDate = "2027-04-10",
                    unitPrice = 4.25,
                    wholesalePrice = 3.10,
                    barcode = "628100100210",
                    quantity = 28, // بديل متوفر ورخيص
                    minStockLevel = 5,
                    category = "مسكنات وخافضات حرارة",
                    preferredSupplier = "شركة الدوائية الوطنية",
                    dailyBurnRate = 2.8
                ),
                Product(
                    name = "بنادول أزرق 500 ملغ",
                    manufacturer = "GSK (جلاكسو سميث كلاين)",
                    activeIngredient = "Paracetamol",
                    dosageForm = "أقراص عادية",
                    strength = "500 mg",
                    unit = "علبة",
                    expiryDate = "2027-08-15",
                    unitPrice = 6.50,
                    wholesalePrice = 4.80,
                    barcode = "628100100211",
                    quantity = 35, // بديل متوفر عالمي
                    minStockLevel = 6,
                    category = "مسكنات وخافضات حرارة",
                    preferredSupplier = "شركة التوريدات الطبية المتحدة",
                    dailyBurnRate = 4.0
                ),
                Product(
                    name = "بنادول إكسترا أقراص",
                    manufacturer = "GSK (جلاكسو سميث كلاين)",
                    activeIngredient = "Paracetamol + Caffeine",
                    dosageForm = "أقراص عادية",
                    strength = "500mg / 65mg",
                    unit = "علبة",
                    expiryDate = "2027-05-18",
                    unitPrice = 7.50,
                    wholesalePrice = 5.20,
                    barcode = "628100100204",
                    quantity = 22,
                    minStockLevel = 5,
                    category = "مسكنات وخافضات حرارة",
                    preferredSupplier = "مستودع أدوية الحياة",
                    dailyBurnRate = 2.0
                ),
                Product(
                    name = "أموكسيسيلين 500 ملغ",
                    manufacturer = "حكمة للأدوية (Hikma)",
                    activeIngredient = "Amoxicillin",
                    dosageForm = "كبسولات",
                    strength = "500 mg",
                    unit = "علبة",
                    expiryDate = "2027-02-14",
                    unitPrice = 12.00,
                    wholesalePrice = 8.50,
                    barcode = "628100100202",
                    quantity = 1, // حرج جداً (ينفد خلال ساعات)
                    minStockLevel = 5,
                    category = "مضادات حيوية",
                    preferredSupplier = "شركة الحكمة للمستودعات",
                    dailyBurnRate = 2.2
                ),
                Product(
                    name = "أيموكس 500 ملغ كبسول",
                    manufacturer = "المصرية الدولية EIPICO",
                    activeIngredient = "Amoxicillin",
                    dosageForm = "كبسولات",
                    strength = "500 mg",
                    unit = "علبة",
                    expiryDate = "2027-06-20",
                    unitPrice = 9.50,
                    wholesalePrice = 6.80,
                    barcode = "628100100212",
                    quantity = 18, // بديل متوفر أرخص
                    minStockLevel = 4,
                    category = "مضادات حيوية",
                    preferredSupplier = "شركة الحكمة للمستودعات",
                    dailyBurnRate = 1.5
                ),
                Product(
                    name = "أوغمنتين 1 جم أقراص",
                    manufacturer = "GSK (جلاكسو سميث كلاين)",
                    activeIngredient = "Amoxicillin + Clavulanic Acid",
                    dosageForm = "أقراص عادية",
                    strength = "1000 mg",
                    unit = "علبة",
                    expiryDate = "2026-08-30",
                    unitPrice = 36.50,
                    wholesalePrice = 27.00,
                    barcode = "628100100203",
                    quantity = 14,
                    minStockLevel = 5,
                    category = "مضادات حيوية",
                    preferredSupplier = "مستودع أدوية الحياة",
                    dailyBurnRate = 1.8
                ),
                Product(
                    name = "كلافوكس 1 جم",
                    manufacturer = "SPIMACO السعودية",
                    activeIngredient = "Amoxicillin + Clavulanic Acid",
                    dosageForm = "أقراص عادية",
                    strength = "1000 mg",
                    unit = "علبة",
                    expiryDate = "2027-03-25",
                    unitPrice = 29.00,
                    wholesalePrice = 21.50,
                    barcode = "628100100213",
                    quantity = 20, // بديل أرخص ومتوفر
                    minStockLevel = 5,
                    category = "مضادات حيوية",
                    preferredSupplier = "شركة الدوائية الوطنية",
                    dailyBurnRate = 1.9
                ),
                Product(
                    name = "بروفين 400 ملغ (إيبوبروفين)",
                    manufacturer = "Abbott (أبوت)",
                    activeIngredient = "Ibuprofen",
                    dosageForm = "أقراص عادية",
                    strength = "400 mg",
                    unit = "علبة",
                    expiryDate = "2027-01-30",
                    unitPrice = 14.50,
                    wholesalePrice = 10.50,
                    barcode = "628100100205",
                    quantity = 15,
                    minStockLevel = 5,
                    category = "مسكنات وخافضات حرارة",
                    preferredSupplier = "شركة أدوية الخليج",
                    dailyBurnRate = 2.0
                ),
                Product(
                    name = "أسبرين بروتكت 100 ملغ",
                    manufacturer = "Bayer (باير الألمانية)",
                    activeIngredient = "Aspirin",
                    dosageForm = "أقراص عادية",
                    strength = "100 mg",
                    unit = "علبة",
                    expiryDate = "2027-11-20",
                    unitPrice = 11.20,
                    wholesalePrice = 8.00,
                    barcode = "628100100206",
                    quantity = 25,
                    minStockLevel = 5,
                    category = "أدوية القلب والتخثر",
                    preferredSupplier = "شركة أدوية الخليج",
                    dailyBurnRate = 1.4
                ),
                Product(
                    name = "وارفارين 2 ملغ أقراص",
                    manufacturer = "Orion Pharma",
                    activeIngredient = "Warfarin",
                    dosageForm = "أقراص عادية",
                    strength = "2 mg",
                    unit = "علبة",
                    expiryDate = "2026-10-15",
                    unitPrice = 18.00,
                    wholesalePrice = 13.50,
                    barcode = "628100100207",
                    quantity = 8,
                    minStockLevel = 4,
                    category = "أدوية القلب والتخثر",
                    preferredSupplier = "شركة التوريدات الطبية المتحدة",
                    dailyBurnRate = 0.8
                ),
                Product(
                    name = "جلوكوفاج 500 ملغ (ميتفورمين)",
                    manufacturer = "Merck (ميرك)",
                    activeIngredient = "Metformin",
                    dosageForm = "أقراص عادية",
                    strength = "500 mg",
                    unit = "علبة",
                    expiryDate = "2027-09-12",
                    unitPrice = 13.80,
                    wholesalePrice = 10.00,
                    barcode = "628100100208",
                    quantity = 2, // حرج جداً ينفد خلال 48 ساعة
                    minStockLevel = 6,
                    category = "أدوية السكري",
                    preferredSupplier = "شركة الدوائية الوطنية",
                    dailyBurnRate = 2.5
                ),
                Product(
                    name = "كونكور 5 ملغ (بيسوبرولول)",
                    manufacturer = "Merck (ميرك)",
                    activeIngredient = "Bisoprolol",
                    dosageForm = "أقراص عادية",
                    strength = "5 mg",
                    unit = "علبة",
                    expiryDate = "2027-07-20",
                    unitPrice = 24.50,
                    wholesalePrice = 18.00,
                    barcode = "628100100209",
                    quantity = 19,
                    minStockLevel = 5,
                    category = "أدوية ضغط الدم والقلب",
                    preferredSupplier = "شركة التوريدات الطبية المتحدة",
                    dailyBurnRate = 1.2
                )
            )
            dao.insertAllProducts(initialProducts)

            // فئات الأدوية الأساسية (Medicine Categories)
            val initialCategories = listOf(
                MedicineCategoryEntity(
                    name = "مسكنات وخافضات حرارة",
                    code = "ANALGESIC",
                    description = "أدوية لتسكين الآلام وخفض درجة الحرارة والحمى ومضادات الالتهاب اللاستيرويدية",
                    iconName = "healing",
                    colorHex = "#10B981",
                    requiresPrescription = false,
                    storageConditions = "تحفظ بدرجة حرارة الغرفة (أقل من 25° مئوية) بعيداً عن الرطوبة",
                    displayOrder = 1
                ),
                MedicineCategoryEntity(
                    name = "مضادات حيوية ومضادات عدوى",
                    code = "ANTIBIOTIC",
                    description = "مضادات البكتيريا والجراثيم وتتطلب وصفة طبية ملزمة والالتزام بالجرعات",
                    iconName = "medication",
                    colorHex = "#EF4444",
                    requiresPrescription = true,
                    storageConditions = "تحفظ في مكان جاف وبارد (الشراب المعلق يحفظ في الثلاجة 2-8° م بعد التحضير)",
                    displayOrder = 2
                ),
                MedicineCategoryEntity(
                    name = "أدوية ضغط الدم والقلب",
                    code = "CARDIOVASCULAR",
                    description = "أدوية تنظيم ضغط الدم الشرياني ودعم عضلة القلب وعلاج قصور القلب",
                    iconName = "favorite",
                    colorHex = "#3B82F6",
                    requiresPrescription = true,
                    storageConditions = "تحفظ في مكان جاف بدرجة حرارة الغرفة بعيداً عن الضوء المباشر",
                    displayOrder = 3
                ),
                MedicineCategoryEntity(
                    name = "أدوية السكري والغدد",
                    code = "DIABETES",
                    description = "أدوية تنظيم سكر الدم الفموية وحقن الإنسولين وأدوية الغدة الدرقية",
                    iconName = "monitor_heart",
                    colorHex = "#8B5CF6",
                    requiresPrescription = true,
                    storageConditions = "أقلام الإنسولين غير المفتوحة تحفظ بالثلاجة (2-8° مئوية) ولا تجمد",
                    displayOrder = 4
                ),
                MedicineCategoryEntity(
                    name = "أدوية الجهاز الهضمي والقولون",
                    code = "GASTROINTESTINAL",
                    description = "مثبطات مضخة البروتون، مضادات الحموضة، وعلاجات القولون العصبي والإسهال",
                    iconName = "restaurant",
                    colorHex = "#F59E0B",
                    requiresPrescription = false,
                    storageConditions = "تحفظ في مكان بارد وجاف أقل من 25° مئوية",
                    displayOrder = 5
                ),
                MedicineCategoryEntity(
                    name = "أدوية الحساسية والجهاز التنفسي",
                    code = "RESPIRATORY",
                    description = "مضادات الهيستامين، بخاخات الربو، وموسعات الشعب الهوائية ومهدئات السعال",
                    iconName = "air",
                    colorHex = "#06B6D4",
                    requiresPrescription = false,
                    storageConditions = "البخاخات تحفظ بعيداً عن أشعة الشمس المباشرة والحرارة المرتفعة",
                    displayOrder = 6
                ),
                MedicineCategoryEntity(
                    name = "فيتامينات ومكملات غذائية",
                    code = "VITAMINS",
                    description = "فيتامينات متعددة، معادن (كالسيوم، حديد، زنك)، ومقويات للمناعة والصحة العامة",
                    iconName = "wb_sunny",
                    colorHex = "#14B8A6",
                    requiresPrescription = false,
                    storageConditions = "تحفظ في عبوات محكمة الإغلاق بدرجة حرارة الغرفة",
                    displayOrder = 7
                ),
                MedicineCategoryEntity(
                    name = "مستحضرات جلدية وموضعية",
                    code = "DERMATOLOGY",
                    description = "كريمات، مراهم، مضادات فطريات جلدية، وعلاجات الحروق والجروح",
                    iconName = "spa",
                    colorHex = "#EC4899",
                    requiresPrescription = false,
                    storageConditions = "تحفظ في مكان بارد وجاف أقل من 25° مئوية",
                    displayOrder = 8
                ),
                MedicineCategoryEntity(
                    name = "عام ومستلزمات طبية",
                    code = "GENERAL",
                    description = "مستلزمات الضماد، المعقمات، والأدوية العامة غير المصنفة",
                    iconName = "medical_services",
                    colorHex = "#64748B",
                    requiresPrescription = false,
                    storageConditions = "تحفظ بدرجة حرارة الغرفة في مكان جاف",
                    displayOrder = 9
                )
            )
            dao.insertAllCategories(initialCategories)

            // 2. Initial Drug Interactions Database
            val interactions = listOf(
                DrugInteractionEntity(
                    ingredientA = "Ibuprofen",
                    ingredientB = "Aspirin",
                    severityLevel = "HIGH",
                    description = "تزامن الإيبوبروفين والأسبرين يزيد خطر نزيف الجهاز الهضمي والتقرحات المعدية ويبطل المفعول القلبي الوقائي للأسبرين.",
                    recommendation = "تجنب التناول المتزامن، واستبدال الإيبوبروفين بالباراسيتامول لتسكين الألم بأمان."
                ),
                DrugInteractionEntity(
                    ingredientA = "Aspirin",
                    ingredientB = "Ibuprofen",
                    severityLevel = "HIGH",
                    description = "تزامن الإيبوبروفين والأسبرين يزيد خطر نزيف الجهاز الهضمي والتقرحات المعدية ويبطل المفعول القلبي الوقائي للأسبرين.",
                    recommendation = "تجنب التناول المتزامن، واستبدال الإيبوبروفين بالباراسيتامول لتسكين الألم بأمان."
                ),
                DrugInteractionEntity(
                    ingredientA = "Paracetamol",
                    ingredientB = "Warfarin",
                    severityLevel = "MEDIUM",
                    description = "استخدام الباراسيتامول بجرعات عالية أو متكررة مع الوارفارين قد يرفع سيولة الدم ومعدل INR ويزيد خطر النزف.",
                    recommendation = "الحد من جرعة الباراسيتامول إلى أقل من 2 غرام يومياً ومراقبة معدل تخثر الدم INR بانتظام."
                ),
                DrugInteractionEntity(
                    ingredientA = "Warfarin",
                    ingredientB = "Paracetamol",
                    severityLevel = "MEDIUM",
                    description = "استخدام الباراسيتامول بجرعات عالية أو متكررة مع الوارفارين قد يرفع سيولة الدم ومعدل INR ويزيد خطر النزف.",
                    recommendation = "الحد من جرعة الباراسيتامول إلى أقل من 2 غرام يومياً ومراقبة معدل تخثر الدم INR بانتظام."
                ),
                DrugInteractionEntity(
                    ingredientA = "Ibuprofen",
                    ingredientB = "Warfarin",
                    severityLevel = "HIGH",
                    description = "تعارض شديد الخطورة: يزيد الإيبوبروفين مع الوارفارين خطر النزيف الحاد بشكل كبير.",
                    recommendation = "ممنوع الجمع إطلاقاً؛ يوصى باستخدام الباراسيتامول كبديل مسكن آمن تحت الإشراف الطبي."
                ),
                DrugInteractionEntity(
                    ingredientA = "Warfarin",
                    ingredientB = "Ibuprofen",
                    severityLevel = "HIGH",
                    description = "تعارض شديد الخطورة: يزيد الإيبوبروفين مع الوارفارين خطر النزيف الحاد بشكل كبير.",
                    recommendation = "ممنوع الجمع إطلاقاً؛ يوصى باستخدام الباراسيتامول كبديل مسكن آمن تحت الإشراف الطبي."
                )
            )
            dao.insertAllInteractions(interactions)

            // 3. Initial Medicine Dosages & Instructions
            val dosages = listOf(
                MedicineDosageEntity(
                    medicineId = 0,
                    medicineName = "باراسيتامول",
                    activeIngredient = "Paracetamol",
                    adultDosageText = "قرص أو قرصان (500-1000 ملجم) كل 6 إلى 8 ساعات عند اللزوم بعد الأكل.",
                    pediatricDosagePerKg = 15.0, // 15 ملغم لكل كجم
                    usageInstructions = "يؤخذ بعد الأكل مع كوب ماء، ولا يؤخذ على معدة فارغة في حال التحسس.",
                    precautions = "الحد الأقصى للبالغين 4000 ملجم يومياً لتجنب الإضرار بوظائف الكبد."
                ),
                MedicineDosageEntity(
                    medicineId = 0,
                    medicineName = "أموكسيسيلين",
                    activeIngredient = "Amoxicillin",
                    adultDosageText = "كبسولة واحدة 500 ملجم كل 8 ساعات بانتظام لمدة 7 أيام متواصلة.",
                    pediatricDosagePerKg = 30.0, // 30 ملغم لكل كجم
                    usageInstructions = "يجب إكمال الكورس العلاجي بالكامل وعدم التوقف حتى بعد الشعور بالتحسن.",
                    precautions = "ممنوع للمرضى الذين لديهم حساسية سابقة للبنسلين أو مشتقاته."
                ),
                MedicineDosageEntity(
                    medicineId = 0,
                    medicineName = "أوغمنتين / كلافوكس",
                    activeIngredient = "Amoxicillin + Clavulanic Acid",
                    adultDosageText = "قرص واحد 1 جم كل 12 ساعة في بداية الوجبة بانتظام لمدة 7-10 أيام.",
                    pediatricDosagePerKg = 35.0,
                    usageInstructions = "يؤخذ مباشرة في بداية تناول الطعام لتقليل اضطرابات الجهاز الهضمي وتحسين الامتصاص.",
                    precautions = "يحفظ في مكان جاف وبارد، ولا تتجاوز الجرعة المقررة بدون استشارة الطبيب."
                ),
                MedicineDosageEntity(
                    medicineId = 0,
                    medicineName = "بروفين (إيبوبروفين)",
                    activeIngredient = "Ibuprofen",
                    adultDosageText = "قرص واحد 400 ملجم كل 8 ساعات بعد الأكل مباشرة.",
                    pediatricDosagePerKg = 10.0,
                    usageInstructions = "يؤخذ حصراً بعد الوجبات مع كوب كامل من الماء أو الحليب لتفادي تهيج المعدة.",
                    precautions = "ممنوع لمرضى قرحة المعدة أو القصور الكلوي، وتجنب الجمع مع الأسبرين."
                ),
                MedicineDosageEntity(
                    medicineId = 0,
                    medicineName = "جلوكوفاج (ميتفورمين)",
                    activeIngredient = "Metformin",
                    adultDosageText = "قرص واحد 500 ملجم مع أو بعد وجبة الإفطار والعشاء مرتين يومياً.",
                    pediatricDosagePerKg = 0.0,
                    usageInstructions = "يؤخذ مع الأكل لتقليل الأعراض الهضمية، والحرص على شرب السوائل بانتظام.",
                    precautions = "متابعة فحوصات وظائف الكلى الدورية وفيتامين B12 كل ستة أشهر."
                )
            )
            dao.insertAllDosages(dosages)

            // 4. Initial Customers & Ledger
            val initialCustomers = listOf(
                CustomerEntity(
                    name = "أبو فهد الشمري",
                    phone = "0501234567",
                    currentBalance = 145.0,
                    creditLimit = 1500.0,
                    notes = "عميل دائم - أدوية الأمراض المزمنة للوالد"
                ),
                CustomerEntity(
                    name = "خالد بن سلطان الدوسري",
                    phone = "0559876543",
                    currentBalance = 320.0,
                    creditLimit = 2000.0,
                    notes = "حساب عائلي شهري"
                ),
                CustomerEntity(
                    name = "أم عبد العزيز المطيري",
                    phone = "0561122334",
                    currentBalance = 0.0,
                    creditLimit = 800.0,
                    notes = "سداد فوري منتظم"
                )
            )
            dao.insertAllCustomers(initialCustomers)

            // Initial Ledger entries for testing
            val initialLedgers = listOf(
                CustomerLedgerEntity(
                    customerId = 1,
                    transactionType = "DEBIT_SALE",
                    amount = 145.0,
                    balanceAfter = 145.0,
                    note = "فاتورة مشتريات علاج السكري والضغط #INV-1092"
                ),
                CustomerLedgerEntity(
                    customerId = 2,
                    transactionType = "DEBIT_SALE",
                    amount = 520.0,
                    balanceAfter = 520.0,
                    note = "أدوية شهرية عائلية #INV-1085"
                ),
                CustomerLedgerEntity(
                    customerId = 2,
                    transactionType = "PAYMENT",
                    amount = 200.0,
                    balanceAfter = 320.0,
                    note = "سداد دفعة نقدية في الصيدلية"
                )
            )
            dao.insertAllLedgerEntries(initialLedgers)

            // 5. Initial Chronic Refill Reminders
            val oneDayMs = 24L * 60 * 60 * 1000
            val initialReminders = listOf(
                RefillReminderEntity(
                    customerId = 1,
                    customerName = "أبو فهد الشمري",
                    customerPhone = "0501234567",
                    medicineName = "جلوكوفاج 500 ملغ (ميتفورمين)",
                    lastRefillDate = System.currentTimeMillis() - (28 * oneDayMs),
                    daysSupply = 30,
                    nextRefillDate = System.currentTimeMillis() + (2 * oneDayMs), // ينتهي بعد يومين!
                    isContacted = false,
                    notes = "العلاج الشهري لمتابعة سكر الدم"
                ),
                RefillReminderEntity(
                    customerId = 2,
                    customerName = "خالد بن سلطان الدوسري",
                    customerPhone = "0559876543",
                    medicineName = "كونكور 5 ملغ (بيسوبرولول)",
                    lastRefillDate = System.currentTimeMillis() - (27 * oneDayMs),
                    daysSupply = 30,
                    nextRefillDate = System.currentTimeMillis() + (3 * oneDayMs), // ينتهي بعد 3 أيام!
                    isContacted = false,
                    notes = "علاج تنظيم ضغط الدم المنتظم"
                ),
                RefillReminderEntity(
                    customerId = 3,
                    customerName = "أم عبد العزيز المطيري",
                    customerPhone = "0561122334",
                    medicineName = "أسبرين بروتكت 100 ملغ",
                    lastRefillDate = System.currentTimeMillis() - (30 * oneDayMs),
                    daysSupply = 30,
                    nextRefillDate = System.currentTimeMillis() - (1 * oneDayMs), // موعده اليوم أو تأخر!
                    isContacted = false,
                    notes = "وقاية القلب والأوعية الدموية"
                )
            )
            dao.insertAllRefillReminders(initialReminders)

            // Seed Users
            val initialUsers = listOf(
                PharmacyUser(
                    name = "د. أحمد الفاروقي",
                    username = "admin",
                    role = "مدير الصيدلية",
                    phone = "0501112233"
                ),
                PharmacyUser(
                    name = "د. سارة المنصور",
                    username = "sara",
                    role = "صيدلي أول",
                    phone = "0554445566"
                ),
                PharmacyUser(
                    name = "محمد العمري",
                    username = "cashier1",
                    role = "أمين صندوق (كاشير)",
                    phone = "0567778899"
                )
            )
            dao.insertAllUsers(initialUsers)

            // Seed Initial Invoices and Sale Items for Analytics
            seedInitialInvoicesIfEmpty(dao)
        }

        suspend fun seedInitialInvoicesIfEmpty(dao: PharmacyDao) {
            if (dao.getAllInvoicesStatic().isNotEmpty()) return
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            val sampleInvoices = listOf(
                SaleInvoice(id = 1, invoiceNumber = "INV-10021", timestamp = now - (6 * dayMs), totalAmount = 145.50, itemCount = 4, paymentMethod = "نقدي", cashierName = "د. أحمد الفاروقي", customerName = "محمد عبد الله"),
                SaleInvoice(id = 2, invoiceNumber = "INV-10022", timestamp = now - (5 * dayMs), totalAmount = 280.00, itemCount = 6, paymentMethod = "بطاقة مدى", cashierName = "د. أحمد الفاروقي", customerName = "سالم القرني"),
                SaleInvoice(id = 3, invoiceNumber = "INV-10023", timestamp = now - (4 * dayMs), totalAmount = 195.00, itemCount = 5, paymentMethod = "نقدي", cashierName = "د. سارة المنصور", customerName = "أم عبد العزيز المطيري"),
                SaleInvoice(id = 4, invoiceNumber = "INV-10024", timestamp = now - (3 * dayMs), totalAmount = 350.25, itemCount = 8, paymentMethod = "بطاقة مدى", cashierName = "د. سارة المنصور", customerName = "خالد بن سلطان الدوسري"),
                SaleInvoice(id = 5, invoiceNumber = "INV-10025", timestamp = now - (2 * dayMs), totalAmount = 420.00, itemCount = 7, paymentMethod = "نقدي", cashierName = "محمد العمري", customerName = "فهد الشمري"),
                SaleInvoice(id = 6, invoiceNumber = "INV-10026", timestamp = now - (1 * dayMs), totalAmount = 510.75, itemCount = 9, paymentMethod = "بطاقة مدى", cashierName = "محمد العمري", customerName = "ياسر العتيبي"),
                SaleInvoice(id = 7, invoiceNumber = "INV-10027", timestamp = now, totalAmount = 390.00, itemCount = 6, paymentMethod = "نقدي", cashierName = "د. أحمد الفاروقي", customerName = "زبون نقدي")
            )

            val sampleItems = listOf(
                SaleItem(invoiceId = 1, productId = 1, productName = "باراسيتامول 500 ملغ", quantity = 3, unitPrice = 12.0, totalPrice = 36.0),
                SaleItem(invoiceId = 1, productId = 2, productName = "بانادول إكسترا", quantity = 2, unitPrice = 18.0, totalPrice = 36.0),
                SaleItem(invoiceId = 2, productId = 3, productName = "أوجمنتين 1 جم", quantity = 3, unitPrice = 48.5, totalPrice = 145.5),
                SaleItem(invoiceId = 2, productId = 1, productName = "باراسيتامول 500 ملغ", quantity = 4, unitPrice = 12.0, totalPrice = 48.0),
                SaleItem(invoiceId = 3, productId = 4, productName = "كونكور 5 ملغ", quantity = 2, unitPrice = 32.0, totalPrice = 64.0),
                SaleItem(invoiceId = 3, productId = 2, productName = "بانادول إكسترا", quantity = 3, unitPrice = 18.0, totalPrice = 54.0),
                SaleItem(invoiceId = 4, productId = 3, productName = "أوجمنتين 1 جم", quantity = 2, unitPrice = 48.5, totalPrice = 97.0),
                SaleItem(invoiceId = 4, productId = 5, productName = "بروفين 400 ملغ", quantity = 4, unitPrice = 19.0, totalPrice = 76.0),
                SaleItem(invoiceId = 4, productId = 6, productName = "أوميبرازول 20 ملغ", quantity = 3, unitPrice = 28.0, totalPrice = 84.0),
                SaleItem(invoiceId = 5, productId = 1, productName = "باراسيتامول 500 ملغ", quantity = 6, unitPrice = 12.0, totalPrice = 72.0),
                SaleItem(invoiceId = 5, productId = 3, productName = "أوجمنتين 1 جم", quantity = 3, unitPrice = 48.5, totalPrice = 145.5),
                SaleItem(invoiceId = 5, productId = 7, productName = "فولتارين 50 ملغ", quantity = 4, unitPrice = 22.0, totalPrice = 88.0),
                SaleItem(invoiceId = 6, productId = 2, productName = "بانادول إكسترا", quantity = 5, unitPrice = 18.0, totalPrice = 90.0),
                SaleItem(invoiceId = 6, productId = 8, productName = "جلوكوفاج 500 ملغ", quantity = 4, unitPrice = 25.0, totalPrice = 100.0),
                SaleItem(invoiceId = 6, productId = 9, productName = "ليبيتور 20 ملغ", quantity = 3, unitPrice = 55.0, totalPrice = 165.0),
                SaleItem(invoiceId = 7, productId = 1, productName = "باراسيتامول 500 ملغ", quantity = 5, unitPrice = 12.0, totalPrice = 60.0),
                SaleItem(invoiceId = 7, productId = 5, productName = "بروفين 400 ملغ", quantity = 3, unitPrice = 19.0, totalPrice = 57.0),
                SaleItem(invoiceId = 7, productId = 6, productName = "أوميبرازول 20 ملغ", quantity = 4, unitPrice = 28.0, totalPrice = 112.0)
            )

            for (inv in sampleInvoices) {
                dao.insertInvoice(inv)
            }
            dao.insertSaleItems(sampleItems)
        }
    }
}
