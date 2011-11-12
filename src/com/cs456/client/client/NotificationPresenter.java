package com.cs456.client.client;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwtphonegap.client.PhoneGap;
import com.googlecode.gwtphonegap.client.notification.AlertCallback;
import com.googlecode.gwtphonegap.client.notification.ConfirmCallback;


public class NotificationPresenter {
        private final Display display;
        private final PhoneGap phoneGap;

        public NotificationPresenter(Display display, PhoneGap phoneGap) {
                this.display = display;
                this.phoneGap = phoneGap;

                bind();

        }

        private void bind() {
                display.getAlertButton().addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                                phoneGap.getNotification().alert("daniel says hi", new AlertCallback() {

                                        @Override
                                        public void onOkButtonClicked() {

                                        }
                                }, "gwt-phonegap", "buttonText");

                        }
                });

                display.getBeepButton().addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                                phoneGap.getNotification().beep(2);

                        }
                });

                display.getVibrateButton().addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                                phoneGap.getNotification().vibrate(2500);

                        }
                });

                display.getConfirmButton().addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {

                                phoneGap.getNotification().confirm("question?", new ConfirmCallback() {

                                        @Override
                                        public void onConfirm(int button) {

                                        }
                                }, "gwt-phonegap");

                        }
                });

        }

        public interface Display {

                public Widget asWidget();

                public HasClickHandlers getBeepButton();

                public HasClickHandlers getAlertButton();

                public HasClickHandlers getConfirmButton();

                public HasClickHandlers getVibrateButton();

        }

        public Display getDisplay() {
                return display;
        }
}