package cn.debubu.mytest.data.repository

/**
 * 频段计算测试工具类
 * 用于验证5G NR和LTE频段计算的正确性
 */
class BandCalculationTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("=== 5G NR 频段计算测试 ===")
            testNrBandCalculation()
            
            println("\n=== LTE 频段计算测试 ===")
            testLteBandCalculation()
        }
        
        /**
         * 测试5G NR频段计算
         */
        private fun testNrBandCalculation() {
            // 测试样例：NR-ARFCN -> 预期频段
            val nrTestCases = listOf(
                // FR1 (Sub-6 GHz)
                Pair(260000, 78),   // n78: 3.5 GHz (1300.1 MHz -> 3500 MHz)
                Pair(290000, 79),   // n79: 4.9 GHz (1450.1 MHz -> 4900 MHz)
                Pair(390000, 78),   // n78: 3.5 GHz (1950.1 MHz -> 3500 MHz)
                Pair(60000, 1),     // n1: 2100 MHz (300.1 MHz -> 2100 MHz)
                Pair(120000, 3),    // n3: 1800 MHz (600.1 MHz -> 1800 MHz)
                Pair(400000, 79),   // n79: 4.9 GHz (2000.1 MHz -> 4900 MHz)
                
                // FR2 (Millimeter Wave)
                Pair(600000, 257),  // n257: 26 GHz (2457.6 MHz -> 26 GHz)
                Pair(800000, 258),  // n258: 28 GHz (2757.6 MHz -> 28 GHz)
                Pair(1000000, 260), // n260: 39 GHz (3057.6 MHz -> 39 GHz)
                Pair(1200000, 261)  // n261: 41 GHz (3357.6 MHz -> 41 GHz)
            )
            
            nrTestCases.forEach { (nrarfcn, expectedBand) ->
                val frequency = getNrFrequency(nrarfcn)
                val band = getNrBand(nrarfcn)
                val result = if (band == expectedBand) "✓" else "✗"
                println("NR-ARFCN: $nrarfcn -> 频率: ${frequency/1000.0} MHz -> 频段: $band (预期: $expectedBand) $result")
            }
        }
        
        /**
         * 测试LTE频段计算
         */
        private fun testLteBandCalculation() {
            // 测试样例：EARFCN -> 预期频段
            val lteTestCases = listOf(
                // FDD频段
                Pair(0, 1),     // n1: 2100 MHz
                Pair(600, 2),   // n2: 1900 MHz
                Pair(1200, 3),  // n3: 1800 MHz
                Pair(2400, 5),  // n5: 850 MHz
                Pair(2750, 7),  // n7: 2600 MHz
                Pair(3450, 8),  // n8: 900 MHz
                
                // TDD频段
                Pair(37750, 38), // n38: 2600 MHz
                Pair(38250, 39), // n39: 1900 MHz
                Pair(38650, 40), // n40: 2300 MHz
                Pair(39650, 41), // n41: 2500 MHz
                
                // 扩展TDD频段
                Pair(65536, 71), // n71: 600 MHz
                Pair(67736, 77), // n77: 3.5 GHz
                Pair(68586, 78), // n78: 3.5 GHz
                Pair(74586, 79)  // n79: 4.9 GHz
            )
            
            lteTestCases.forEach { (earfcn, expectedBand) ->
                val frequency = getLteFrequency(earfcn)
                val band = getLteBand(earfcn)
                val result = if (band == expectedBand) "✓" else "✗"
                println("EARFCN: $earfcn -> 频率: ${frequency/1000.0} MHz -> 频段: $band (预期: $expectedBand) $result")
            }
        }
        
        // 从CellularRepository复制的测试方法
        private fun getNrBand(nrarfcn: Int): Int {
            val freq = getNrFrequency(nrarfcn) / 1000.0
            return when {
                // FR1 (Sub-6 GHz)
                nrarfcn in 0..599999 -> when {
                    freq in 2110.0..2170.0 -> 1
                    freq in 1850.0..1910.0 -> 2
                    freq in 1710.0..1785.0 -> 3
                    freq in 1710.0..1755.0 -> 4
                    freq in 824.0..849.0 -> 5
                    freq in 830.0..840.0 -> 6
                    freq in 2500.0..2570.0 -> 7
                    freq in 880.0..915.0 -> 8
                    freq in 1749.9..1784.9 -> 9
                    freq in 2110.0..2170.0 -> 10
                    freq in 1427.9..1452.9 -> 11
                    freq in 698.0..716.0 -> 12
                    freq in 777.0..787.0 -> 13
                    freq in 788.0..798.0 -> 14
                    freq in 1920.0..1980.0 -> 18
                    freq in 832.0..862.0 -> 19
                    freq in 832.0..862.0 -> 20
                    freq in 1447.9..1462.9 -> 21
                    freq in 3410.0..3490.0 -> 22
                    freq in 2180.0..2200.0 -> 23
                    freq in 1625.5..1660.5 -> 24
                    freq in 1930.0..1990.0 -> 25
                    freq in 850.0..869.0 -> 26
                    freq in 807.0..824.0 -> 27
                    freq in 703.0..748.0 -> 28
                    freq in 717.0..728.0 -> 29
                    freq in 2305.0..2315.0 -> 30
                    freq in 3300.0..3800.0 -> 78
                    freq in 4400.0..5000.0 -> 79
                    else -> 0
                }
                // FR2 (Millimeter Wave)
                nrarfcn in 600000..2016666 -> when {
                    freq in 24250.0..27500.0 -> 257
                    freq in 24250.0..27500.0 -> 258
                    freq in 37000.0..40000.0 -> 260
                    freq in 40500.0..43500.0 -> 261
                    freq in 60000.0..71000.0 -> 262
                    freq in 50400.0..52600.0 -> 271
                    else -> 0
                }
                else -> 0
            }
        }
        
        private fun getNrFrequency(nrarfcn: Int): Int {
            return when (nrarfcn) {
                in 0..599999 -> 5 * nrarfcn + 100
                in 600000..2016666 -> 15 * (nrarfcn - 600000) + 2457600
                else -> 0
            }
        }
        
        private fun getLteBand(earfcn: Int): Int {
            return when {
                // FDD频段
                earfcn in 0..599 -> 1     // n1: 2100 MHz
                earfcn in 600..1199 -> 2   // n2: 1900 MHz
                earfcn in 1200..1949 -> 3  // n3: 1800 MHz
                earfcn in 1950..2399 -> 4  // n4: AWS-1
                earfcn in 2400..2649 -> 5  // n5: 850 MHz
                earfcn in 2650..2749 -> 6  // n6: 800 MHz
                earfcn in 2750..3449 -> 7  // n7: 2600 MHz
                earfcn in 3450..3799 -> 8  // n8: 900 MHz
                earfcn in 3800..4149 -> 9  // n9: 1800 MHz
                earfcn in 4150..4749 -> 10 // n10: AWS-1
                earfcn in 4750..4949 -> 11 // n11: 1500 MHz
                earfcn in 5010..5179 -> 12 // n12: 700 MHz
                earfcn in 5180..5279 -> 13 // n13: 700 MHz
                earfcn in 5280..5379 -> 14 // n14: 700 MHz
                earfcn in 5730..5849 -> 17 // n17: 700 MHz
                earfcn in 5850..5999 -> 18 // n18: 800 MHz
                earfcn in 6000..6149 -> 19 // n19: 800 MHz
                earfcn in 6150..6449 -> 20 // n20: 800 MHz
                earfcn in 6450..6599 -> 21 // n21: 1500 MHz
                earfcn in 6600..7399 -> 22 // n22: 3500 MHz
                earfcn in 7500..7699 -> 23 // n23: 2100 MHz
                earfcn in 7700..8039 -> 24 // n24: 1600 MHz
                earfcn in 8040..8689 -> 25 // n25: 1900 MHz
                earfcn in 8690..9039 -> 26 // n26: 850 MHz
                earfcn in 9040..9209 -> 27 // n27: 800 MHz
                earfcn in 9210..9659 -> 28 // n28: 700 MHz
                earfcn in 9660..9769 -> 29 // n29: 700 MHz
                earfcn in 9770..9869 -> 30 // n30: 2300 MHz
                earfcn in 9870..9919 -> 31 // n31: 450 MHz
                earfcn in 9920..10359 -> 32 // n32: 1500 MHz
                
                // TDD频段
                earfcn in 37750..38249 -> 38 // n38: 2600 MHz
                earfcn in 38250..38649 -> 39 // n39: 1900 MHz
                earfcn in 38650..39649 -> 40 // n40: 2300 MHz
                earfcn in 39650..41589 -> 41 // n41: 2500 MHz
                earfcn in 41590..43589 -> 42 // n42: 3500 MHz
                earfcn in 43590..45589 -> 43 // n43: 3700 MHz
                earfcn in 45590..46589 -> 44 // n44: 700 MHz
                earfcn in 46590..47589 -> 45 // n45: 1500 MHz
                earfcn in 47590..48589 -> 46 // n46: 5 GHz
                earfcn in 48590..49589 -> 47 // n47: 6 GHz
                earfcn in 50300..51499 -> 48 // n48: CBRS
                earfcn in 51500..52549 -> 49 // n49: 3.5 GHz
                
                // 其他频段
                earfcn in 65536..66435 -> 71 // n71: 600 MHz
                earfcn in 66436..67235 -> 72 // n72: 450 MHz
                earfcn in 67236..67335 -> 73 // n73: 450 MHz
                earfcn in 67586..67635 -> 74 // n74: 1500 MHz
                earfcn in 67636..67685 -> 75 // n75: 1500 MHz
                earfcn in 67686..67735 -> 76 // n76: 1500 MHz
                earfcn in 67736..68585 -> 77 // n77: 3.5 GHz
                earfcn in 68586..74585 -> 78 // n78: 3.5 GHz
                earfcn in 74586..75585 -> 79 // n79: 4.9 GHz
                
                else -> 0
            }
        }
        
        private fun getLteFrequency(earfcn: Int): Int {
            return when {
                earfcn in 0..35999 -> {
                    val fdl = 2110.0 + 0.010 * earfcn
                    (fdl * 1000).toInt()
                }
                earfcn in 36000..59999 -> {
                    val fcenter = 2300.0 + 0.010 * (earfcn - 36000)
                    (fcenter * 1000).toInt()
                }
                earfcn in 60000..65535 -> {
                    val fcenter = 2500.0 + 0.010 * (earfcn - 60000)
                    (fcenter * 1000).toInt()
                }
                earfcn in 65536..262143 -> {
                    val fcenter = 2500.0 + 0.010 * (earfcn - 65536)
                    (fcenter * 1000).toInt()
                }
                else -> 0
            }
        }
    }
}