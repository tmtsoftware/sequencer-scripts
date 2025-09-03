package shared

import esw.ocs.dsl.core.script
import esw.ocs.dsl.highlevel.models.IRIS
import csw.prefix.models.Subsystem.AOESW
import csw.prefix.models.Prefix
import csw.prefix.models.Subsystem
import esw.ocs.dsl.params.*

object aoesw {
    val subsystem = "AOESW"
    object aosq {
        val componentName = "aosq"
        val prefix = aoeswPrefixStr(componentName)

        val lgsfSeqComponentName = "aosq.lgsf"
        val lgsfPrefix = aoeswPrefixStr(lgsfSeqComponentName)
    object rpg {
        val componentName = "rpg"
        val prefix = aoeswPrefixStr(componentName)
    }
    object psfr {
        val componentName = "psfr"
        val prefix = aoeswPrefixStr(componentName)
    }

    }
}

object nfiraos {
    val subsystem = "nfiraos"
    object lgsTrombone {
        val componentName = "lgsTrombone"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object power {
        val componentName = "power"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object at {
        val componentName = "at"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object bs {
        val componentName = "bs"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object dm {
        val componentName = "dm"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object ism {
        val componentName = "ism"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object nscu {
        val componentName = "nscu"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object nsen {
        val componentName = "nsen"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object ssLgs {
        val componentName = "ssLgs"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object ssNgs {
        val componentName = "ssNgs"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object tts {
        val componentName = "tts"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object vnwAdc {
        val componentName = "vnwAdc"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object vnwFieldStop {
        val componentName = "vnwFieldStop"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object vnwFsm {
        val componentName = "vnwFsm"
        val prefix = nfiraosPrefixStr(componentName)
    }
    object vnwSsm {
        val componentName = "vnwSsm"
        val prefix = nfiraosPrefixStr(componentName)
    }

}

object iris {
    val subsystem = "iris"
    object oiwfs1 {
        val componentName = "oiwfs1"
        object detector {
            val componentName = oiwfs1.componentName+"."+"detector"
            val prefix = irisPrefixStr(componentName)
        }
        object poa {
            val componentName = oiwfs1.componentName+"."+"poa"
            val prefix = irisPrefixStr(componentName)
        }
        object adc {
            val componentName = oiwfs1.componentName+"."+"adc"
            val prefix = irisPrefixStr(componentName)
        }
    }
    object oiwfs2 {
        val componentName = "oiwfs2"
        object detector {
            val componentName = oiwfs2.componentName+"."+"detector"
            val prefix = irisPrefixStr(componentName)
        }
        object poa {
            val componentName = oiwfs2.componentName+"."+"poa"
            val prefix = irisPrefixStr(componentName)
        }
        object adc {
            val componentName = oiwfs2.componentName+"."+"adc"
            val prefix = irisPrefixStr(componentName)
        }

    }
    object oiwfs3 {
        val componentName = "oiwfs3"
        object detector {
            val componentName = oiwfs3.componentName+"."+"detector"
            val prefix = irisPrefixStr(componentName)
        }
        object poa {
            val componentName = oiwfs3.componentName+"."+"poa"
            val prefix = irisPrefixStr(componentName)
        }
        object adc {
            val componentName = oiwfs3.componentName+"."+"adc"
            val prefix = irisPrefixStr(componentName)
        }

    }

}


fun prefixStr(subsystem: String, compName: String) = "$subsystem.$compName"
fun aoeswPrefixStr(compName: String) = prefixStr(aoesw.subsystem, compName)
fun nfiraosPrefixStr(compName: String) = prefixStr(nfiraos.subsystem, compName)
fun irisPrefixStr(compName: String) = prefixStr(iris.subsystem, compName)
