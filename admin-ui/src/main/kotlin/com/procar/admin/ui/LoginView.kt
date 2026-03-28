package com.procar.admin.ui

import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route

@Route("login")
@PageTitle("Login | Procar Admin")
class LoginView : VerticalLayout(), BeforeEnterObserver {
    
    private val login = LoginForm()
    
    init {
        addClassName("login-view")
        setSizeFull()
        alignItems = Alignment.CENTER
        justifyContentMode = JustifyContentMode.CENTER

        login.action = "login"
        login.isForgotPasswordButtonVisible = false
        
        add(H1("Procar Admin"), login)
    }
    
    override fun beforeEnter(event: BeforeEnterEvent?) {
        if (event?.location?.queryParameters?.parameters?.get("error")?.isNotEmpty() == true) {
            login.isError = true
        }
    }
}
