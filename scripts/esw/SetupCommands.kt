package esw

import csw.params.commands.CommandName
import csw.params.commands.Setup
import csw.params.core.models.*
import csw.params.javadsl.JUnits
import csw.prefix.models.Prefix
import csw.time.core.models.TAITime
import csw.time.core.models.UTCTime
import esw.ocs.dsl.params.*
import java.time.Instant
import java.util.*

class SetupCommands {
    fun getIrisCommand2(source: Prefix): Setup {
        val booleanKeyParam = booleanKey("BooleanKey1").set(true, false)
        val byteKeyParam = byteKey("ByteKey1").set(10, 20)
        val charKeyParam = charKey("CharKey1").set('A','B')//TODO Not working
        val shortKeyParam = shortKey("ShortKey1").set(30, 40)
        val longKeyParam = longKey("LongKey1").set(50, 56)
        val temperatureParam = intKey("IntKey1", JUnits.kelvin).set(70, 80, 90)

        return Setup(source, CommandName("iris-command2"), Optional.empty())
                .madd(booleanKeyParam, byteKeyParam, shortKeyParam, longKeyParam, temperatureParam)
    }


    fun getIrisCommand3(source: Prefix): Setup {
        val floatKeyParam = floatKey("FloatKey1").set(90.3f, 100.7f)
        val doubleParam = doubleKey("DoubleKey1").set(110.5, 120.6)
        val utcParam = utcTimeKey("UTCTimeKey1").set(UTCTime(Instant.ofEpochMilli(0)), UTCTime(Instant.parse("2017-09-04T19:00:00.123456789Z")))
        val byteArrayParam = byteArrayKey("ByteArrayKey1").set(arrayData(arrayOf(1, 2)))
        val shortArrayParam = shortArrayKey("ShortArrayKey1").set(arrayData(arrayOf(3, 4)))
        val longArrayParam = longArrayKey("LongArrayKey1").set(arrayData(arrayOf(5, 6)))

        val eqCoord = JEqCoord.make("12:13:14.15", "-30:31:32.3")
        val solarSystemCoord = Coords.SolarSystemCoord(Coords.Tag("BASE"), JCoords.Venus())
        val minorPlanetCoord = Coords.MinorPlanetCoord(Coords.Tag("GUIDER1"), 2000.0, Angle(9), Angle(2), Angle(100), 1.4, 0.234, Angle(220))
        val cometCoord = Coords.CometCoord(Coords.Tag("BASE"), 2000.0, Angle(90), Angle(2), Angle(100), 1.4, 0.234)
        val altAzCoord = Coords.AltAzCoord(Coords.Tag("BASE"), Angle(301), JAngle.degree(42.5))
        val coordKeyParam = coordKey("CoordKey1").set(eqCoord, solarSystemCoord, minorPlanetCoord, cometCoord, altAzCoord)

        return Setup(source, CommandName("iris-command3"), Optional.empty())
                .madd(floatKeyParam, doubleParam, utcParam, byteArrayParam, shortArrayParam, longArrayParam, coordKeyParam)
    }

    fun getTcsCommand2(source: Prefix): Setup {
        val intArrayKeyParam = intArrayKey("IntArrayKey1").set(arrayData(arrayOf(7, 8)))
        val floatArrayKeyParam = floatArrayKey("FloatArrayKey1").set(arrayData(arrayOf(9.5f, 10.4f)))
        val doubleArrayKeyParam = doubleArrayKey("DoubleArrayKey1").set(arrayData(arrayOf(11.5, 12.4)))
        val byteMatrixKeyParam = byteMatrixKey("ByteMatrix1").set(matrixData(arrayOf(1, 2), arrayOf(3, 4)))
        val shortMatrixKeyParam = shortMatrixKey("ShortMatrix1").set(matrixData(arrayOf(4, 5), arrayOf(6, 7)))
        val taiTimeParam = taiTimeKey("TAITimeKey1").set(TAITime(Instant.ofEpochMilli(0)), TAITime(Instant.parse("2017-09-04T19:00:00.123456789Z")))

        return Setup(source, CommandName("tcs-command2"), Optional.empty())
                .madd(intArrayKeyParam, floatArrayKeyParam, doubleArrayKeyParam, byteMatrixKeyParam, shortMatrixKeyParam, taiTimeParam)
    }

    fun getTcsCommand3(source: Prefix): Setup {
        val longMatrixKeyParam = longMatrixKey("LongMatrix1").set(matrixData(arrayOf(8, 9), arrayOf(10, 11)))
        val intMatrixKeyParam = intMatrixKey("IntMatrix1").set(matrixData(arrayOf(12, 13), arrayOf(14, 15)))
        val floatMatrixKeyParam = floatMatrixKey("FloatMatrix1").set(matrixData(arrayOf(16.5f, 17.4f), arrayOf(18.2f, 19.0f)))
        val doubleMatrixKeyParam = doubleMatrixKey("DoubleMatrix1").set(matrixData(arrayOf(20.4, 21.1), arrayOf(22.7, 23.8)))
        val radecParam = raDecKey("RaDecKey1").set(RaDec(7.3, 12.1))
        val choiceParam = choiceKey("ChoiceKey1", choicesOf("A", "B", "C")).set(Choice("A"), Choice("C"))
        val stringParam = stringKey("StringKey1").set("ABC", "PGRSTUV", "abcdefghi")

        return Setup(source, CommandName("tcs-command3"), Optional.empty())
                .madd(longMatrixKeyParam, intMatrixKeyParam, floatMatrixKeyParam, doubleMatrixKeyParam, radecParam, choiceParam, stringParam)
    }

}
