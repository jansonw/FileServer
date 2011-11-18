package com.cs456.client.client;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwtphonegap.client.PhoneGap;

public class MainPresenter {

        private final Display display;
        private final NotificationPresenter notificationPresenter;
        private final PhoneGap phoneGap;
        private final FilePresenter filePresenter;
        private final ChildBrowserPresenter childBrowserPresenter;

        public MainPresenter(Display display, PhoneGap phoneGap, NotificationPresenter notificationPresenter, 
        						FilePresenter filePresenter, ChildBrowserPresenter childBrowserPresenter) {
                this.display = display;
                this.phoneGap = phoneGap;
                this.notificationPresenter = notificationPresenter;
                this.filePresenter = filePresenter;
                this.childBrowserPresenter = childBrowserPresenter;

                bind();

        }

        private void bind() {
                display.getExitButton().addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                                phoneGap.exitApp();

                        }
                });

        }

        public interface Display {
                public Widget asWidget();

                public HasWidgets getContainer();

                public HasClickHandlers getExitButton();

        }

        public Display getDisplay() {
                return display;
        }

        public void start() {
                display.getContainer().add(filePresenter.getDisplay().asWidget());
                display.getContainer().add(notificationPresenter.getDisplay().asWidget());
                display.getContainer().add(childBrowserPresenter.getDisplay().asWidget());

        }
}