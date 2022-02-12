/*
 * Copyright (c) 2018-2021 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/GuiFrame.kt
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.gui

import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.flattener.FlattenerListener
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.Closeable
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import javax.swing.*
import kotlin.concurrent.withLock
import net.kyori.adventure.text.Component as AdvTextComponent

class GuiFrame : JFrame("LuckPerms - MessageLog"), GuiSender, Closeable {
    val panel = JPanel()
    val group = GroupLayout(panel)
    private val lines = ConcurrentLinkedDeque<Collection<Component>>()
    private val lineCounter = AtomicInteger()
    private var rml = false

    private fun addLine(line: Collection<Component>): Collection<Component>? {
        lines.add(line)
        return when {
            rml -> {
                lines.removeFirst()
            }
            lineCounter.getAndIncrement() > 200 -> {
                rml = true
                lines.removeFirst()
            }
            else -> null
        }
    }

    var mouseOnDialog: Boolean = false
    var mouseOnText: Boolean = false
    val hov = JWindow(this)
    val hovP = JPanel()
    val scrollpane: JScrollPane

    private fun tryCloseDialog() {
        service.schedule({
            if (!mouseOnDialog && !mouseOnText) {
                hov.isVisible = false
            }
        }, 20, TimeUnit.MILLISECONDS)
    }

    private var currentOnHov: LabelWithHov? = null
    private val mvLst = object : MouseAdapter() {
        override fun mouseEntered(e: MouseEvent) {
            if (!this@GuiFrame.hasFocus()) {
                return
            }
            val comp = e.component
            if (comp is LabelWithHov) {
                mouseOnText = true
                val c = currentOnHov
                currentOnHov = comp

                if (c !== comp) {
                    hovP.removeAll()
                    comp.cmpsOnHov.forEach { hovP.add(it) }
                    //hov.setSize(500, 200)
                    hov.pack()
                }
                val pointer = MouseInfo.getPointerInfo()
                val location = pointer.location

                hov.setLocation(location.x, location.y)
                hov.isVisible = true
                hov.opacity = 1f
            }
        }

        override fun mouseExited(e: MouseEvent) {
            mouseOnText = false
            tryCloseDialog()
        }
    }
    private val service = Executors.newScheduledThreadPool(2, object : ThreadFactory {
        private val counter = AtomicInteger()
        override fun newThread(r: Runnable): Thread {
            return Thread(r, "LuckPerms - GUI Scheduler#" + counter.getAndIncrement())
        }
    })
    private val lock = ReentrantLock()

    @Volatile
    private var scroolToBottom = false

    private fun updatePanel(before: (() -> Unit)?) {
        SwingUtilities.invokeLater {
            lock.withLock {
                before?.invoke()

                val bbr = scrollpane.verticalScrollBar
                val v = bbr.model
                if (v.value + bbr.height == v.maximum) {
                    scroolToBottom = true
                }

                val g = group.createParallelGroup()
                val g2 = group.createSequentialGroup()

                lines.forEach { line ->
                    val gx = group.createSequentialGroup()
                    val gx2 = group.createParallelGroup()
                    line.forEach {
                        if (mvLst !in it.mouseListeners) {
                            it.addMouseListener(mvLst)
                        }
                        gx.addComponent(it)
                        gx2.addComponent(it)
                    }
                    g.addGroup(gx)
                    g2.addGroup(gx2)
                }

                group.setHorizontalGroup(g)
                group.setVerticalGroup(g2)
            }
        }
    }

    init {
        hov.setSize(200, 200)
        hov.background = Color.BLACK
        hov.add(hovP)
        hovP.background = Color.BLACK
        panel.layout = group
        panel.background = Color.BLACK
        panel.foreground = Color.WHITE
        group.honorsVisibility = false
        setSize(400, 300)

        hov.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                mouseOnDialog = true
            }

            override fun mouseExited(e: MouseEvent?) {
                mouseOnDialog = false
                tryCloseDialog()
            }
        })

        val bar = JScrollPane(panel)
        bar.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS

        /*val bar = scrollpane.verticalScrollBar
        val v = bar.model
        println("Z: ${v.value}, ${v.maximum}, ${v.value + bar.height}")
        */
        bar.verticalScrollBar.addAdjustmentListener { event ->
            if (scroolToBottom) {
                scroolToBottom = false
                event.adjustable.value = event.adjustable.maximum
            }
        }
        add(bar)
        scrollpane = bar
        this.defaultCloseOperation = DO_NOTHING_ON_CLOSE
    }

    //////////// public control functions //////////////////////

    @Synchronized
    override fun sendMsg(msg: AdvTextComponent) {
        val armed = mutableListOf<Collection<Component>>()
        msg.buildToLines().forEach { l -> addLine(l)?.let { armed.add(it) } }
        updatePanel {
            armed.asSequence().flatMap { it.asSequence() }.forEach {
                panel.remove(it)
            }
        }
    }

    override fun close() {
        service.shutdown()
        hov.dispose()
        this.dispose()
    }

    override val isSupported: Boolean get() = true

    @Synchronized
    override fun clearScreen() {
        lines.clear()
        lineCounter.set(0)
        rml = false
        updatePanel {
            panel.removeAll()
        }
    }
}

class LabelWithHov : JLabel {
    constructor() : super()
    constructor(text: String) : super(text)

    var cmpsOnHov: MutableCollection<Component> = mutableListOf()
}

fun Collection<Collection<Component>>.buildToPanel() = JPanel().also { panel ->
    val group = GroupLayout(panel)
    panel.layout = group

    val g = group.createParallelGroup()
    val g2 = group.createSequentialGroup()

    this.forEach { line ->
        val gx = group.createSequentialGroup()
        val gx2 = group.createParallelGroup()
        line.forEach {
            gx.addComponent(it)
            gx2.addComponent(it)
        }
        g.addGroup(gx)
        g2.addGroup(gx2)
    }

    group.setHorizontalGroup(g)
    group.setVerticalGroup(g2)
}

fun AdvTextComponent.buildToLines(): Collection<Collection<Component>> {
    return Ftr(false).also {
        ComponentFlattener.basic().flatten(this, it)
    }.resp
}

private fun AdvTextComponent.nestedLines(): Collection<Collection<Component>> {
    return Ftr(true).also {
        ComponentFlattener.basic().flatten(this, it)
    }.resp
}

class Ftr(private val noNested: Boolean = false) : FlattenerListener {
    companion object {
        private val DECORATIONS = TextDecoration.values()
    }

    class StyleState {
        var color: TextColor? = null

        //val decorations: MutableSet<TextDecoration> = EnumSet.noneOf(TextDecoration::class.java)
        var hoverEvent: HoverEvent<*>? = null
        fun apply(style: Style) {
            style.color()?.let { this.color = it }
            style.hoverEvent()?.let {
                this.hoverEvent = it
            }
            /*DECORATIONS.forEach { dec ->
                when (style.decoration(dec)) {
                    TextDecoration.State.FALSE -> decorations.remove(dec)
                    TextDecoration.State.TRUE -> decorations.add(dec)
                    else -> {
                    }
                }
            }*/
        }

        fun clear() {
            color = null
            //decorations.clear()
            hoverEvent = null
        }

        fun emitStyle(label: JLabel) {
            label.foreground = color?.let {
                Color(it.value())
            } ?: Color.WHITE
        }

        fun copy(o: StyleState) {
            color = o.color
            hoverEvent = o.hoverEvent
        }
    }

    private var styles = arrayOfNulls<StyleState>(8)
    private var head = -1
    override fun pushStyle(style: Style) {
        val idx = ++head
        if (idx >= styles.size) {
            styles = styles.copyOf(styles.size * 2)
        }
        var ste = styles[idx]
        if (ste == null) {
            styles[idx] = StyleState().also { ste = it }
        }
        val s = ste!!
        if (idx > 0) {
            s.copy(styles[idx - 1]!!)
        }
        s.apply(style)
    }

    override fun popStyle(style: Style) {
        head--
    }


    val resp = mutableListOf<MutableList<Component>>()
    var currentLine = mutableListOf<Component>().also { resp.add(it) }

    override fun component(text: String) {
        if (text.isEmpty()) return
        val iterator = text.lines().iterator()
        val style = styles[head]!!
        val hoverEvent = style.takeUnless { noNested }?.hoverEvent?.let { evt ->
            val cp = evt.value()
            if (cp is AdvTextComponent) {
                cp.nestedLines().buildToPanel().also { it.background = Color.BLACK }
            } else {
                null
            }
        }

        fun hvBel(text: String): JLabel {
            return if (hoverEvent != null) LabelWithHov(text).also { it.cmpsOnHov.add(hoverEvent!!) }
            else JLabel(text)
        }
        iterator.next().let { firstLine ->
            if (firstLine.isBlank()) return@let
            val next = hvBel(firstLine)
            style.emitStyle(next)
            currentLine.add(next)
        }
        for (otherLine in iterator) {
            val line = mutableListOf<Component>()
            currentLine = line
            resp.add(line)

            val next = hvBel(otherLine)
            style.emitStyle(next)
            line.add(next)
        }
    }
}

interface GuiSender {
    fun sendMsg(msg: AdvTextComponent)
    val isSupported: Boolean get() = false
    fun clearScreen() {}
}

object GuiSenderNoAction : GuiSender {
    override fun sendMsg(msg: AdvTextComponent) {
    }
}

val guiSender: GuiSender by lazy {
    if (runCatching {
            Desktop.isDesktopSupported() && !GraphicsEnvironment.isHeadless()
        }.getOrElse { false }) {
        GuiFrame().also { it.isVisible = true }
    } else GuiSenderNoAction
}
