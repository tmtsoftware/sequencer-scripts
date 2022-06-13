@file:Repository("https://jitpack.io/")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-dsl-kt_2.13:adc26faf3413a9e70a6627c397563e88ea04afb6")
@file:DependsOn("com.github.tmtsoftware.esw:esw-ocs-app_2.13:adc26faf3413a9e70a6627c397563e88ea04afb6")

import csw.params.commands.SequenceCommand
import csw.params.core.models.ObsId
import esw.ocs.dsl.params.Params
import esw.ocs.dsl.params.values

fun getObsId(command: SequenceCommand): ObsId {
    val maybeObsId = command.maybeObsId()
    return if(maybeObsId.isDefined) maybeObsId.get()
    else throw Error("ObsId not found in ${command.commandName()}")
}

fun Params.format(): String {
    return params().joinToString(", ") { x -> "${x.keyName()} -> ${x.values}" }
}
