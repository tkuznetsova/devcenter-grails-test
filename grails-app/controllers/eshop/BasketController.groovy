package eshop

import authentication.AuthenticationController
import org.springframework.dao.DataIntegrityViolationException

class BasketController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

	def create(id) {
		def basketInstance = new Basket(params)
		params.basketCost=0.0
		params.itemCount=0
		basketInstance.properties = params
		basketInstance.version = 1	
		
		def userInstance = new AuthenticationController().findUser(id)
		basketInstance.user = userInstance
		println "${session.user.login} AuthenticationController-signup says user not found"
		if(basketInstance.save()) {
		
		}else {
			println "${session.user.login} AuthenticationController-signup-Basket-create says basketInstance unsaved"
		}
		return basketInstance
	}
	
	def findBasket(id) {
		def basketInstance = new Basket().find {id}
		return basketInstance 
	}
	
	def save() {
		def basketInstance = new Basket(params)
		if (!basketInstance.save(flush: true)) {
			render(view: "create", model: [basketInstance: basketInstance])
			return
		}

		flash.message = message(code: 'default.created.message', args: [message(code: 'basket.label', default: 'Basket'), basketInstance.id])
		redirect(action: "show", id: basketInstance.id)
	}

	def list(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		[basketInstanceList: Basket.list(params), basketInstanceTotal: Basket.count()]
	}
	
	def show(Long id) {
		println params
		Basket basketInstance = Basket.get(params.id)
		println basketInstance.user.name
		if (!basketInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'basket.label', default: 'Basket'), id])
			redirect(action: "list")
			return
		}
		return [basketInstance: basketInstance ]
	}

	def edit(Long id) {
		println params
		Basket basketInstance = Basket.get(params.id)
		
		if (!basketInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'basket.label', default: 'Basket'), id])
			redirect(action: "list")
			return
		}
		return [Basket : basketInstance ]
	}

	def update(Long id, Long version) {
		println params
		Basket basketInstance = Basket.get(params.id)
		basketInstance.properties = params
		basketInstance.save()
		redirect(action:'list')
		if (!basketInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'basket.label', default: 'Basket'), id])
			redirect(action: "list")
			return
		}
		if (version != null) {
			if (basketInstance.version > version) {
				basketIstance.errors.rejectValue("version", "default.optimistic.locking.failure",
						  [message(code: 'basket.label', default: 'Basket')] as Object[],
						  "Another user has updated this Basket while you were editing")
				render(view: "edit", model: [basketInstance: basketInstance])
				return
			}
		}

		basketInstance.properties = params

		if (!basketInstance.save(flush: true)) {
			render(view: "edit", model: [basketItemInstance: basketInstance])
			return
		}

		flash.message = message(code: 'default.updated.message', args: [message(code: 'basket.label', default: 'Basket'), basketInstance.id])
		redirect(action: "show", id: basketInstance.id)
	}

     def delete(Long id) {
		println params
        def basketInstance = Basket.get(id)
        if (!basketInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'basket.label', default: 'Basket'), id])
            redirect(action: "list")
            return
        }

        try {
            basketInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'basket.label', default: 'Basket'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'basket.label', default: 'Basket'), id])
            redirect(action: "show", id: id)
        }
    }
	
	def addPurchase(Long id) {
		BasketItem p = BasketItem.addToBasket( id )
		println "${p.good.price}"
		
		def b = Basket.get(session.user.id)
		.addToPurchase(p)
		.save()
		
		// Add one more item
		b.itemCount += p.quantity
		p.cost = p.good.price * p.quantity
		// Add the price of product
		b.basketCost += p.cost
		
		session.basket = b
		println "Count 1: ${b.count()}"
		println "Count 2: ${b.itemCount}"
		
		if (p.save()) {
		println "Purchase added to the basket"
		redirect(controller:"basket", action:"show", id:"${session.user.id}")
		}
		else {
		println "Purchase NOT added to the basket"
		redirect(controller:"basket", action:"show", id:"${session.user.id}")
		}
		}
		
		def removePurchase(Long id) {
			println params
			def p = BasketItem.get( id )
			//println "${p.good.name}"
			
			Basket b = Basket.get(session.user.id)
println b.basketCost
println b.itemCount
			// Delete one entire nomenclature
			b.itemCount -= p.quantity
println p.quantity
println p.cost

			// Delete the price of product
			b.basketCost -= p.cost
println b.basketCost

			
			// The price cannot be less than 0
			if (b.basketCost < 0)
				b.basketCost = 0
			if (b.itemCount < 0)
				b.itemCount = 0
				
			b.removeFromPurchase(p)
			.save()
			
			p.delete()
			session.basket = b
			println "Count 1: ${b.count()}"
			println "Count 2: ${b.itemCount}"
println b.basketCost
			if (p.save()) {
			redirect(controller:"basket", action:"show", id:"${session.user.id}")
			}
			else {
			redirect(controller:"basket", action:"show", id:"${session.user.id}")
			}
		}
}
