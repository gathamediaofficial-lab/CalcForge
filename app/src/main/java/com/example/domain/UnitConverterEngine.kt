package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs

enum class UnitGroup(val displayName: String) {
    GENERAL("General & Finance"),
    PHYSICS("Physics"),
    CHEMISTRY("Chemistry")
}

enum class UnitCategory(
    val displayName: String,
    val shortName: String,
    val group: UnitGroup,
    val isOfflineReference: Boolean = false
) {
    CURRENCY("Currency", "Currency", UnitGroup.GENERAL, isOfflineReference = true),
    LENGTH("Length", "Length", UnitGroup.GENERAL),
    MASS("Mass & Weight", "Mass", UnitGroup.GENERAL),
    TEMPERATURE("Temperature", "Temp", UnitGroup.GENERAL),
    AREA("Area", "Area", UnitGroup.GENERAL),
    VOLUME("Volume", "Volume", UnitGroup.GENERAL),
    SPEED("Speed", "Speed", UnitGroup.GENERAL),
    TIME("Time", "Time", UnitGroup.GENERAL),
    DATA("Data Storage", "Data", UnitGroup.GENERAL),
    // Physics
    FORCE("Force", "Force", UnitGroup.PHYSICS),
    ENERGY("Energy & Work", "Energy", UnitGroup.PHYSICS),
    POWER("Power", "Power", UnitGroup.PHYSICS),
    PRESSURE("Pressure", "Pressure", UnitGroup.PHYSICS),
    ANGLE("Angle", "Angle", UnitGroup.PHYSICS),
    FREQUENCY("Frequency", "Freq", UnitGroup.PHYSICS),
    ELECTRIC_CHARGE("Electric Charge", "Charge", UnitGroup.PHYSICS),
    RADIATION("Radiation Dose", "Radiation", UnitGroup.PHYSICS),
    // Chemistry
    AMOUNT_OF_SUBSTANCE("Amount of Substance", "Moles", UnitGroup.CHEMISTRY),
    CONCENTRATION("Molar Concentration", "Molarity", UnitGroup.CHEMISTRY),
    DENSITY("Density", "Density", UnitGroup.CHEMISTRY),
    VISCOSITY("Dynamic Viscosity", "Viscosity", UnitGroup.CHEMISTRY)
}

data class MeasurementUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val category: UnitCategory,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

data class ConversionEntry(
    val unit: MeasurementUnit,
    val formattedValue: String,
    val rawValue: Double
)

object UnitConverterEngine {

    private fun linearUnit(
        id: String,
        name: String,
        symbol: String,
        category: UnitCategory,
        factorToBase: Double
    ): MeasurementUnit = MeasurementUnit(
        id = id,
        name = name,
        symbol = symbol,
        category = category,
        toBase = { it * factorToBase },
        fromBase = { it / factorToBase }
    )

    // Currency helper: base = USD; ratePerUsd = how many units of this currency equal 1 USD
    private fun currencyUnit(
        id: String,
        name: String,
        symbol: String,
        ratePerUsd: Double
    ): MeasurementUnit = MeasurementUnit(
        id = id,
        name = name,
        symbol = symbol,
        category = UnitCategory.CURRENCY,
        toBase = { it / ratePerUsd },
        fromBase = { it * ratePerUsd }
    )

    val unitsByCategory: Map<UnitCategory, List<MeasurementUnit>> = mapOf(
        // --- GENERAL & FINANCE ---
        UnitCategory.CURRENCY to listOf(
            currencyUnit("usd", "US Dollar", "USD ($)", 1.0),
            currencyUnit("eur", "Euro", "EUR (€)", 0.92),
            currencyUnit("gbp", "British Pound", "GBP (£)", 0.79),
            currencyUnit("inr", "Indian Rupee", "INR (₹)", 84.40),
            currencyUnit("jpy", "Japanese Yen", "JPY (¥)", 154.0),
            currencyUnit("cny", "Chinese Yuan", "CNY (¥)", 7.24),
            currencyUnit("cad", "Canadian Dollar", "CAD (C$)", 1.39),
            currencyUnit("aud", "Australian Dollar", "AUD (A$)", 1.53),
            currencyUnit("chf", "Swiss Franc", "CHF", 0.88),
            currencyUnit("sgd", "Singapore Dollar", "SGD (S$)", 1.34),
            currencyUnit("aed", "UAE Dirham", "AED", 3.6725),
            currencyUnit("krw", "South Korean Won", "KRW (₩)", 1395.0),
            currencyUnit("brl", "Brazilian Real", "BRL (R$)", 5.78),
            currencyUnit("mxn", "Mexican Peso", "MXN", 20.15),
            currencyUnit("sar", "Saudi Riyal", "SAR", 3.75),
            currencyUnit("zar", "South African Rand", "ZAR", 18.10),
            currencyUnit("sek", "Swedish Krona", "SEK", 10.85),
            currencyUnit("nzd", "New Zealand Dollar", "NZD", 1.69),
            currencyUnit("hkd", "Hong Kong Dollar", "HKD", 7.78),
            currencyUnit("try", "Turkish Lira", "TRY (₺)", 34.45)
        ),
        UnitCategory.LENGTH to listOf(
            linearUnit("nm", "Nanometer", "nm", UnitCategory.LENGTH, 1e-9),
            linearUnit("um", "Micrometer", "μm", UnitCategory.LENGTH, 1e-6),
            linearUnit("mm", "Millimeter", "mm", UnitCategory.LENGTH, 0.001),
            linearUnit("cm", "Centimeter", "cm", UnitCategory.LENGTH, 0.01),
            linearUnit("m", "Meter", "m", UnitCategory.LENGTH, 1.0),
            linearUnit("km", "Kilometer", "km", UnitCategory.LENGTH, 1000.0),
            linearUnit("in", "Inch", "in", UnitCategory.LENGTH, 0.0254),
            linearUnit("ft", "Foot", "ft", UnitCategory.LENGTH, 0.3048),
            linearUnit("yd", "Yard", "yd", UnitCategory.LENGTH, 0.9144),
            linearUnit("mi", "Mile", "mi", UnitCategory.LENGTH, 1609.344),
            linearUnit("nmi", "Nautical Mile", "nmi", UnitCategory.LENGTH, 1852.0),
            linearUnit("au", "Astronomical Unit", "AU", UnitCategory.LENGTH, 1.495978707e11),
            linearUnit("ly", "Light-Year", "ly", UnitCategory.LENGTH, 9.4607304725808e15)
        ),
        UnitCategory.MASS to listOf(
            linearUnit("ug", "Microgram", "μg", UnitCategory.MASS, 1e-9),
            linearUnit("mg", "Milligram", "mg", UnitCategory.MASS, 1e-6),
            linearUnit("g", "Gram", "g", UnitCategory.MASS, 0.001),
            linearUnit("kg", "Kilogram", "kg", UnitCategory.MASS, 1.0),
            linearUnit("t", "Metric Ton", "t", UnitCategory.MASS, 1000.0),
            linearUnit("oz", "Ounce", "oz", UnitCategory.MASS, 0.028349523125),
            linearUnit("lb", "Pound", "lb", UnitCategory.MASS, 0.45359237),
            linearUnit("st", "Stone", "st", UnitCategory.MASS, 6.35029318),
            linearUnit("amu", "Atomic Mass Unit", "u", UnitCategory.MASS, 1.6605390666e-27)
        ),
        UnitCategory.TEMPERATURE to listOf(
            MeasurementUnit(
                id = "c",
                name = "Celsius",
                symbol = "°C",
                category = UnitCategory.TEMPERATURE,
                toBase = { it },
                fromBase = { it }
            ),
            MeasurementUnit(
                id = "f",
                name = "Fahrenheit",
                symbol = "°F",
                category = UnitCategory.TEMPERATURE,
                toBase = { (it - 32.0) * 5.0 / 9.0 },
                fromBase = { it * 9.0 / 5.0 + 32.0 }
            ),
            MeasurementUnit(
                id = "k",
                name = "Kelvin",
                symbol = "K",
                category = UnitCategory.TEMPERATURE,
                toBase = { it - 273.15 },
                fromBase = { it + 273.15 }
            ),
            MeasurementUnit(
                id = "r",
                name = "Rankine",
                symbol = "°R",
                category = UnitCategory.TEMPERATURE,
                toBase = { (it - 491.67) * 5.0 / 9.0 },
                fromBase = { (it + 273.15) * 9.0 / 5.0 }
            )
        ),
        UnitCategory.AREA to listOf(
            linearUnit("mm2", "Square Millimeter", "mm²", UnitCategory.AREA, 1e-6),
            linearUnit("cm2", "Square Centimeter", "cm²", UnitCategory.AREA, 1e-4),
            linearUnit("m2", "Square Meter", "m²", UnitCategory.AREA, 1.0),
            linearUnit("ha", "Hectare", "ha", UnitCategory.AREA, 10000.0),
            linearUnit("km2", "Square Kilometer", "km²", UnitCategory.AREA, 1e6),
            linearUnit("in2", "Square Inch", "in²", UnitCategory.AREA, 0.00064516),
            linearUnit("ft2", "Square Foot", "ft²", UnitCategory.AREA, 0.09290304),
            linearUnit("yd2", "Square Yard", "yd²", UnitCategory.AREA, 0.83612736),
            linearUnit("ac", "Acre", "ac", UnitCategory.AREA, 4046.8564224),
            linearUnit("mi2", "Square Mile", "mi²", UnitCategory.AREA, 2589988.110336)
        ),
        UnitCategory.VOLUME to listOf(
            linearUnit("ul", "Microliter", "μL", UnitCategory.VOLUME, 1e-6),
            linearUnit("ml", "Milliliter", "mL", UnitCategory.VOLUME, 0.001),
            linearUnit("l", "Liter", "L", UnitCategory.VOLUME, 1.0),
            linearUnit("m3", "Cubic Meter", "m³", UnitCategory.VOLUME, 1000.0),
            linearUnit("tsp", "Teaspoon (US)", "tsp", UnitCategory.VOLUME, 0.00492892159375),
            linearUnit("tbsp", "Tablespoon (US)", "tbsp", UnitCategory.VOLUME, 0.01478676478125),
            linearUnit("floz", "Fluid Ounce (US)", "fl oz", UnitCategory.VOLUME, 0.0295735295625),
            linearUnit("cup", "Cup (US)", "cup", UnitCategory.VOLUME, 0.2365882365),
            linearUnit("pt", "Pint (US)", "pt", UnitCategory.VOLUME, 0.473176473),
            linearUnit("qt", "Quart (US)", "qt", UnitCategory.VOLUME, 0.946352946),
            linearUnit("gal", "Gallon (US)", "gal", UnitCategory.VOLUME, 3.785411784)
        ),
        UnitCategory.SPEED to listOf(
            linearUnit("mps", "Meters per Second", "m/s", UnitCategory.SPEED, 1.0),
            linearUnit("kmh", "Kilometers per Hour", "km/h", UnitCategory.SPEED, 1.0 / 3.6),
            linearUnit("mph", "Miles per Hour", "mph", UnitCategory.SPEED, 0.44704),
            linearUnit("kn", "Knot", "kn", UnitCategory.SPEED, 1852.0 / 3600.0),
            linearUnit("fps", "Feet per Second", "ft/s", UnitCategory.SPEED, 0.3048),
            linearUnit("mach", "Mach (Std Atm)", "Ma", UnitCategory.SPEED, 340.29),
            linearUnit("c_light", "Speed of Light", "c", UnitCategory.SPEED, 299792458.0)
        ),
        UnitCategory.TIME to listOf(
            linearUnit("ns", "Nanosecond", "ns", UnitCategory.TIME, 1e-9),
            linearUnit("us", "Microsecond", "μs", UnitCategory.TIME, 1e-6),
            linearUnit("ms", "Millisecond", "ms", UnitCategory.TIME, 0.001),
            linearUnit("s", "Second", "s", UnitCategory.TIME, 1.0),
            linearUnit("min", "Minute", "min", UnitCategory.TIME, 60.0),
            linearUnit("h", "Hour", "h", UnitCategory.TIME, 3600.0),
            linearUnit("d", "Day", "d", UnitCategory.TIME, 86400.0),
            linearUnit("wk", "Week", "wk", UnitCategory.TIME, 604800.0),
            linearUnit("fn", "Fortnight", "fn", UnitCategory.TIME, 1209600.0),
            linearUnit("mo", "Month (30.44d avg)", "mo", UnitCategory.TIME, 2629746.0),
            linearUnit("qtr", "Quarter (3 mo)", "qtr", UnitCategory.TIME, 7889238.0),
            linearUnit("yr", "Year (365.25d)", "yr", UnitCategory.TIME, 31557600.0),
            linearUnit("dec", "Decade", "dec", UnitCategory.TIME, 315576000.0),
            linearUnit("cen", "Century", "cen", UnitCategory.TIME, 3155760000.0),
            linearUnit("kyr", "Millennium", "kyr", UnitCategory.TIME, 31557600000.0)
        ),
        UnitCategory.DATA to listOf(
            linearUnit("bit", "Bit", "b", UnitCategory.DATA, 0.125),
            linearUnit("b", "Byte", "B", UnitCategory.DATA, 1.0),
            linearUnit("kb", "Kilobyte (SI)", "KB", UnitCategory.DATA, 1e3),
            linearUnit("mb", "Megabyte (SI)", "MB", UnitCategory.DATA, 1e6),
            linearUnit("gb", "Gigabyte (SI)", "GB", UnitCategory.DATA, 1e9),
            linearUnit("tb", "Terabyte (SI)", "TB", UnitCategory.DATA, 1e12),
            linearUnit("pb", "Petabyte (SI)", "PB", UnitCategory.DATA, 1e15),
            linearUnit("kib", "Kibibyte", "KiB", UnitCategory.DATA, 1024.0),
            linearUnit("mib", "Mebibyte", "MiB", UnitCategory.DATA, 1048576.0),
            linearUnit("gib", "Gibibyte", "GiB", UnitCategory.DATA, 1073741824.0)
        ),

        // --- PHYSICS ---
        UnitCategory.FORCE to listOf(
            linearUnit("mn", "Millinewton", "mN", UnitCategory.FORCE, 0.001),
            linearUnit("n", "Newton", "N", UnitCategory.FORCE, 1.0),
            linearUnit("kn", "Kilonewton", "kN", UnitCategory.FORCE, 1000.0),
            linearUnit("dyn", "Dyne", "dyn", UnitCategory.FORCE, 1e-5),
            linearUnit("kgf", "Kilogram-force", "kgf", UnitCategory.FORCE, 9.80665),
            linearUnit("lbf", "Pound-force", "lbf", UnitCategory.FORCE, 4.4482216152605),
            linearUnit("pdl", "Poundal", "pdl", UnitCategory.FORCE, 0.138254954376)
        ),
        UnitCategory.ENERGY to listOf(
            linearUnit("j", "Joule", "J", UnitCategory.ENERGY, 1.0),
            linearUnit("kj", "Kilojoule", "kJ", UnitCategory.ENERGY, 1000.0),
            linearUnit("mj", "Megajoule", "MJ", UnitCategory.ENERGY, 1e6),
            linearUnit("erg", "Erg", "erg", UnitCategory.ENERGY, 1e-7),
            linearUnit("cal", "Calorie", "cal", UnitCategory.ENERGY, 4.184),
            linearUnit("kcal", "Kilocalorie", "kcal", UnitCategory.ENERGY, 4184.0),
            linearUnit("wh", "Watt-hour", "Wh", UnitCategory.ENERGY, 3600.0),
            linearUnit("kwh", "Kilowatt-hour", "kWh", UnitCategory.ENERGY, 3_600_000.0),
            linearUnit("btu", "British Thermal Unit", "BTU", UnitCategory.ENERGY, 1055.05585262),
            linearUnit("ev", "Electronvolt", "eV", UnitCategory.ENERGY, 1.602176634e-19),
            linearUnit("mev", "Mega-electronvolt", "MeV", UnitCategory.ENERGY, 1.602176634e-13)
        ),
        UnitCategory.POWER to listOf(
            linearUnit("mw", "Milliwatt", "mW", UnitCategory.POWER, 0.001),
            linearUnit("w", "Watt", "W", UnitCategory.POWER, 1.0),
            linearUnit("kw", "Kilowatt", "kW", UnitCategory.POWER, 1000.0),
            linearUnit("megaw", "Megawatt", "MW", UnitCategory.POWER, 1e6),
            linearUnit("gw", "Gigawatt", "GW", UnitCategory.POWER, 1e9),
            linearUnit("hp", "Horsepower (Mech)", "hp", UnitCategory.POWER, 745.69987158227),
            linearUnit("ps", "Metric Horsepower", "PS", UnitCategory.POWER, 735.49875),
            linearUnit("btuh", "BTU per Hour", "BTU/h", UnitCategory.POWER, 0.29307107)
        ),
        UnitCategory.PRESSURE to listOf(
            linearUnit("pa", "Pascal", "Pa", UnitCategory.PRESSURE, 1.0),
            linearUnit("kpa", "Kilopascal", "kPa", UnitCategory.PRESSURE, 1000.0),
            linearUnit("mpa", "Megapascal", "MPa", UnitCategory.PRESSURE, 1e6),
            linearUnit("bar", "Bar", "bar", UnitCategory.PRESSURE, 100_000.0),
            linearUnit("mbar", "Millibar", "mbar", UnitCategory.PRESSURE, 100.0),
            linearUnit("atm", "Standard Atmosphere", "atm", UnitCategory.PRESSURE, 101_325.0),
            linearUnit("torr", "Torr", "Torr", UnitCategory.PRESSURE, 101325.0 / 760.0),
            linearUnit("psi", "Pounds per Sq Inch", "psi", UnitCategory.PRESSURE, 6894.757293168),
            linearUnit("mmhg", "Millimeter of Mercury", "mmHg", UnitCategory.PRESSURE, 133.322387415)
        ),
        UnitCategory.ANGLE to listOf(
            linearUnit("deg", "Degree", "°", UnitCategory.ANGLE, 1.0),
            linearUnit("rad", "Radian", "rad", UnitCategory.ANGLE, 180.0 / PI),
            linearUnit("grad", "Gradian", "gon", UnitCategory.ANGLE, 0.9),
            linearUnit("turn", "Full Turn", "rev", UnitCategory.ANGLE, 360.0),
            linearUnit("arcmin", "Arcminute", "′", UnitCategory.ANGLE, 1.0 / 60.0),
            linearUnit("arcsec", "Arcsecond", "″", UnitCategory.ANGLE, 1.0 / 3600.0)
        ),
        UnitCategory.FREQUENCY to listOf(
            linearUnit("hz", "Hertz", "Hz", UnitCategory.FREQUENCY, 1.0),
            linearUnit("khz", "Kilohertz", "kHz", UnitCategory.FREQUENCY, 1e3),
            linearUnit("mhz", "Megahertz", "MHz", UnitCategory.FREQUENCY, 1e6),
            linearUnit("ghz", "Gigahertz", "GHz", UnitCategory.FREQUENCY, 1e9),
            linearUnit("thz", "Terahertz", "THz", UnitCategory.FREQUENCY, 1e12),
            linearUnit("rpm", "Revolutions per Min", "rpm", UnitCategory.FREQUENCY, 1.0 / 60.0)
        ),
        UnitCategory.ELECTRIC_CHARGE to listOf(
            linearUnit("c", "Coulomb", "C", UnitCategory.ELECTRIC_CHARGE, 1.0),
            linearUnit("mc", "Millicoulomb", "mC", UnitCategory.ELECTRIC_CHARGE, 1e-3),
            linearUnit("uc", "Microcoulomb", "μC", UnitCategory.ELECTRIC_CHARGE, 1e-6),
            linearUnit("nc", "Nanocoulomb", "nC", UnitCategory.ELECTRIC_CHARGE, 1e-9),
            linearUnit("mah", "Milliampere-hour", "mAh", UnitCategory.ELECTRIC_CHARGE, 3.6),
            linearUnit("ah", "Ampere-hour", "Ah", UnitCategory.ELECTRIC_CHARGE, 3600.0),
            linearUnit("faraday", "Faraday", "F", UnitCategory.ELECTRIC_CHARGE, 96485.33212),
            linearUnit("elem_e", "Elementary Charge", "e", UnitCategory.ELECTRIC_CHARGE, 1.602176634e-19)
        ),
        UnitCategory.RADIATION to listOf(
            linearUnit("sv", "Sievert", "Sv", UnitCategory.RADIATION, 1.0),
            linearUnit("msv", "Millisievert", "mSv", UnitCategory.RADIATION, 1e-3),
            linearUnit("usv", "Microsievert", "μSv", UnitCategory.RADIATION, 1e-6),
            linearUnit("rem", "Roentgen Eq Man", "rem", UnitCategory.RADIATION, 0.01),
            linearUnit("mrem", "Millirem", "mrem", UnitCategory.RADIATION, 1e-5),
            linearUnit("gy", "Gray", "Gy", UnitCategory.RADIATION, 1.0),
            linearUnit("rad_dose", "Rad", "rad", UnitCategory.RADIATION, 0.01)
        ),

        // --- CHEMISTRY ---
        UnitCategory.AMOUNT_OF_SUBSTANCE to listOf(
            linearUnit("mol", "Mole", "mol", UnitCategory.AMOUNT_OF_SUBSTANCE, 1.0),
            linearUnit("mmol", "Millimole", "mmol", UnitCategory.AMOUNT_OF_SUBSTANCE, 1e-3),
            linearUnit("umol", "Micromole", "μmol", UnitCategory.AMOUNT_OF_SUBSTANCE, 1e-6),
            linearUnit("nmol", "Nanomole", "nmol", UnitCategory.AMOUNT_OF_SUBSTANCE, 1e-9),
            linearUnit("pmol", "Picomole", "pmol", UnitCategory.AMOUNT_OF_SUBSTANCE, 1e-12),
            linearUnit("kmol", "Kilomole", "kmol", UnitCategory.AMOUNT_OF_SUBSTANCE, 1000.0),
            linearUnit("lbmol", "Pound-mole", "lb-mol", UnitCategory.AMOUNT_OF_SUBSTANCE, 453.59237)
        ),
        UnitCategory.CONCENTRATION to listOf(
            linearUnit("mol_l", "Molar (mol/L)", "M", UnitCategory.CONCENTRATION, 1.0),
            linearUnit("mmol_l", "Millimolar (mmol/L)", "mM", UnitCategory.CONCENTRATION, 1e-3),
            linearUnit("umol_l", "Micromolar (μmol/L)", "μM", UnitCategory.CONCENTRATION, 1e-6),
            linearUnit("nmol_l", "Nanomolar (nmol/L)", "nM", UnitCategory.CONCENTRATION, 1e-9),
            linearUnit("mol_m3", "Mole per Cubic Meter", "mol/m³", UnitCategory.CONCENTRATION, 1e-3),
            linearUnit("mol_cm3", "Mole per Cubic Cm", "mol/cm³", UnitCategory.CONCENTRATION, 1000.0)
        ),
        UnitCategory.DENSITY to listOf(
            linearUnit("kg_m3", "Kilogram per m³", "kg/m³", UnitCategory.DENSITY, 1.0),
            linearUnit("g_cm3", "Gram per cm³", "g/cm³", UnitCategory.DENSITY, 1000.0),
            linearUnit("g_ml", "Gram per Milliliter", "g/mL", UnitCategory.DENSITY, 1000.0),
            linearUnit("g_l", "Gram per Liter", "g/L", UnitCategory.DENSITY, 1.0),
            linearUnit("mg_ml", "Milligram per mL", "mg/mL", UnitCategory.DENSITY, 1.0),
            linearUnit("lb_ft3", "Pound per Cubic Foot", "lb/ft³", UnitCategory.DENSITY, 16.01846337),
            linearUnit("lb_gal", "Pound per Gallon (US)", "lb/gal", UnitCategory.DENSITY, 119.826427)
        ),
        UnitCategory.VISCOSITY to listOf(
            linearUnit("pas", "Pascal-second", "Pa·s", UnitCategory.VISCOSITY, 1.0),
            linearUnit("mpas", "Millipascal-second", "mPa·s", UnitCategory.VISCOSITY, 0.001),
            linearUnit("cp", "Centipoise", "cP", UnitCategory.VISCOSITY, 0.001),
            linearUnit("poise", "Poise", "P", UnitCategory.VISCOSITY, 0.1),
            linearUnit("lb_fts", "Pound per Foot-Sec", "lb/(ft·s)", UnitCategory.VISCOSITY, 1.48816394)
        )
    )

    fun defaultPair(category: UnitCategory): Pair<MeasurementUnit, MeasurementUnit> {
        val list = unitsByCategory.getValue(category)
        return when (category) {
            UnitCategory.CURRENCY -> list.first { it.id == "usd" } to list.first { it.id == "eur" }
            UnitCategory.LENGTH -> list.first { it.id == "km" } to list.first { it.id == "mi" }
            UnitCategory.MASS -> list.first { it.id == "kg" } to list.first { it.id == "lb" }
            UnitCategory.TEMPERATURE -> list.first { it.id == "c" } to list.first { it.id == "f" }
            UnitCategory.AREA -> list.first { it.id == "m2" } to list.first { it.id == "ft2" }
            UnitCategory.VOLUME -> list.first { it.id == "l" } to list.first { it.id == "gal" }
            UnitCategory.SPEED -> list.first { it.id == "kmh" } to list.first { it.id == "mph" }
            UnitCategory.TIME -> list.first { it.id == "wk" } to list.first { it.id == "d" }
            UnitCategory.DATA -> list.first { it.id == "gb" } to list.first { it.id == "mb" }
            UnitCategory.FORCE -> list.first { it.id == "n" } to list.first { it.id == "lbf" }
            UnitCategory.ENERGY -> list.first { it.id == "kcal" } to list.first { it.id == "kj" }
            UnitCategory.POWER -> list.first { it.id == "kw" } to list.first { it.id == "hp" }
            UnitCategory.PRESSURE -> list.first { it.id == "bar" } to list.first { it.id == "psi" }
            UnitCategory.ANGLE -> list.first { it.id == "deg" } to list.first { it.id == "rad" }
            UnitCategory.FREQUENCY -> list.first { it.id == "mhz" } to list.first { it.id == "ghz" }
            UnitCategory.ELECTRIC_CHARGE -> list.first { it.id == "mah" } to list.first { it.id == "c" }
            UnitCategory.RADIATION -> list.first { it.id == "msv" } to list.first { it.id == "rem" }
            UnitCategory.AMOUNT_OF_SUBSTANCE -> list.first { it.id == "mol" } to list.first { it.id == "mmol" }
            UnitCategory.CONCENTRATION -> list.first { it.id == "mol_l" } to list.first { it.id == "mmol_l" }
            UnitCategory.DENSITY -> list.first { it.id == "g_cm3" } to list.first { it.id == "kg_m3" }
            UnitCategory.VISCOSITY -> list.first { it.id == "cp" } to list.first { it.id == "pas" }
        }
    }

    fun convert(value: Double, from: MeasurementUnit, to: MeasurementUnit): Double {
        if (from.id == to.id) return value
        val base = from.toBase(value)
        val result = to.fromBase(base)
        return if (abs(result) < 1e-15) 0.0 else result
    }

    fun convertToAll(value: Double, from: MeasurementUnit): List<ConversionEntry> {
        val units = unitsByCategory[from.category].orEmpty()
        return units.filter { it.id != from.id }.map { target ->
            val converted = convert(value, from, target)
            ConversionEntry(
                unit = target,
                formattedValue = formatConvertedValue(converted),
                rawValue = converted
            )
        }
    }

    fun formulaSummary(from: MeasurementUnit, to: MeasurementUnit): String {
        val oneToTarget = convert(1.0, from, to)
        return "1 ${from.symbol} = ${formatConvertedValue(oneToTarget)} ${to.symbol}"
    }

    fun formatConvertedValue(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "—"
        val clean = if (value == -0.0 || abs(value) < 1e-15) 0.0 else value
        if (clean == 0.0) return "0"
        val absVal = abs(clean)
        val symbols = DecimalFormatSymbols(Locale.US)
        if (absVal >= 1e10 || absVal < 1e-5) {
            return DecimalFormat("0.######E0", symbols).format(clean)
        }
        val bd = BigDecimal.valueOf(clean)
            .round(MathContext(9, RoundingMode.HALF_UP))
            .stripTrailingZeros()
        val df = DecimalFormat("#,##0.########", symbols)
        df.maximumFractionDigits = 8
        return df.format(bd)
    }
}
