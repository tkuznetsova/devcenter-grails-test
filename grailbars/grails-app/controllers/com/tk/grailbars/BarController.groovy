package com.tk.grailbars

import org.springframework.dao.DataIntegrityViolationException

class BarController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [barInstanceList: Bar.list(params), barInstanceTotal: Bar.count()]
    }

    def create() {
        [barInstance: new Bar(params)]
    }

    def save() {
        def barInstance = new Bar(params)
        if (!barInstance.save(flush: true)) {
            render(view: "create", model: [barInstance: barInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'bar.label', default: 'Bar'), barInstance.id])
        redirect(action: "show", id: barInstance.id)
    }

    def show(Long id) {
        def barInstance = Bar.get(id)
        if (!barInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'bar.label', default: 'Bar'), id])
            redirect(action: "list")
            return
        }

        [barInstance: barInstance]
    }

    def edit(Long id) {
        def barInstance = Bar.get(id)
        if (!barInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'bar.label', default: 'Bar'), id])
            redirect(action: "list")
            return
        }

        [barInstance: barInstance]
    }

    def update(Long id, Long version) {
        def barInstance = Bar.get(id)
        if (!barInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'bar.label', default: 'Bar'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (barInstance.version > version) {
                barInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'bar.label', default: 'Bar')] as Object[],
                          "Another user has updated this Bar while you were editing")
                render(view: "edit", model: [barInstance: barInstance])
                return
            }
        }

        barInstance.properties = params

        if (!barInstance.save(flush: true)) {
            render(view: "edit", model: [barInstance: barInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'bar.label', default: 'Bar'), barInstance.id])
        redirect(action: "show", id: barInstance.id)
    }

    def delete(Long id) {
        def barInstance = Bar.get(id)
        if (!barInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'bar.label', default: 'Bar'), id])
            redirect(action: "list")
            return
        }

        try {
            barInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'bar.label', default: 'Bar'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'bar.label', default: 'Bar'), id])
            redirect(action: "show", id: id)
        }
    }
}
