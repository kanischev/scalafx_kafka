package ru.sbt.guice

import java.util.concurrent.atomic.AtomicReference

import com.google.inject.multibindings.Multibinder
import com.google.inject.{AbstractModule, Guice, Injector, Module}

import scala.annotation.tailrec
import scala.collection.JavaConverters._

object InjectorHolder {
  private var modules: Seq[Module] = Seq()

  @tailrec
  private[guice] def installModule(module: Module) {
    val oldInjector = injector.get
    modules = modules :+ module
    if (!injector.compareAndSet(oldInjector, Guice.createInjector(modules.asJava))) {
      installModule(module)
    }
  }

  private[guice] val injector: AtomicReference[Injector] = new AtomicReference[Injector]

  def injectorEnsureGet: Injector = injector.get
    .ensuring(_ != null, "Injector wasn't set. Are you forgot to declare InjectionInitializer?")
}

trait InjectionConfigurator {
  def install(module: Module) {
    InjectorHolder.installModule(module)
  }

  def installAll(modules: Seq[Module]) {
    modules.foreach(install)
  }
}

trait Injection {
  private val injector = InjectorHolder.injectorEnsureGet
  injector.injectMembers(this)
}

abstract class ExtendedAbstractModule extends AbstractModule {
  protected def bindSet[T](classes: Iterable[Class[_ <: T]], bindToClass: Class[T]) {
    val multiBinder = Multibinder.newSetBinder(binder, bindToClass)
    classes.foreach(clazz => multiBinder addBinding() to clazz)
  }
}