package zlc.season.rxdownload3.core

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.internal.operators.maybe.MaybeToPublisher.INSTANCE
import java.io.File

class LocalMissionBox : MissionBox {
    private val SET = mutableSetOf<RealMission>()

    override fun create(mission: Mission): Flowable<Status> {
        val realMission = SET.find { it.actual == mission }

        return if (realMission != null) {
            realMission.getProcessor()
        } else {
            val new = RealMission(mission)
            SET.add(new)
            new.getProcessor()
        }
    }

    override fun start(mission: Mission): Maybe<Any> {
        val realMission = SET.find { it.actual == mission } ?:
                return Maybe.error(Throwable("Mission not create"))

        return realMission.start()
    }

    override fun stop(mission: Mission): Maybe<Any> {
        val realMission = SET.find { it.actual == mission } ?:
                return Maybe.error(Throwable("Mission not create"))

        return realMission.stop()
    }

    override fun startAll(): Maybe<Any> {
        val arrays = mutableListOf<Maybe<Any>>()
        SET.forEach { arrays.add(it.start()) }
        return Flowable.fromIterable(arrays)
                .flatMap(INSTANCE, true)
                .lastElement()
    }

    override fun stopAll(): Maybe<Any> {
        val arrays = mutableListOf<Maybe<Any>>()
        SET.forEach { arrays.add(it.stop()) }
        return Flowable.fromIterable(arrays)
                .flatMap(INSTANCE)
                .lastElement()
    }

    override fun getFile(mission: Mission): Maybe<File> {
        val real = RealMission(mission)
        return real.getFile()
    }
}
