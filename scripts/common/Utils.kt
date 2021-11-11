package common

import csw.params.commands.SequenceCommand
import csw.params.core.models.ObsId

fun getObsId(command: SequenceCommand): ObsId {
    val maybeObsId = command.maybeObsId()
    return if(maybeObsId.isDefined) maybeObsId.get()
    else throw Error("ObsId not found in ${command.commandName()}")
}