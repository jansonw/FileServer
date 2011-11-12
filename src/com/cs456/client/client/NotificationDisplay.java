package com.cs456.client.client;

import com.cs456.client.client.NotificationPresenter.Display;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;


public class NotificationDisplay implements Display {

        private DisclosurePanel main;
        private FlowPanel content;
        private Button alertButton;
        private Button vibrateButton;
        private Button beepButton;
        private Button confirmButton;

        public NotificationDisplay() {
                main = new DisclosurePanel("Notification");

                content = new FlowPanel();

                content.add(new HTML("Notification"));

                alertButton = new Button("Alert");
                content.add(alertButton);

                confirmButton = new Button("Confirm");
                content.add(confirmButton);

                vibrateButton = new Button("Vibrate");
                content.add(vibrateButton);

                beepButton = new Button("Beep");
                content.add(beepButton);

                main.add(content);
        }

        @Override
        public HasClickHandlers getBeepButton() {
                return beepButton;
        }

        @Override
        public HasClickHandlers getAlertButton() {
                return alertButton;
        }

        @Override
        public HasClickHandlers getVibrateButton() {
                return vibrateButton;
        }

        @Override
        public Widget asWidget() {
                return main;
        }

        /* (non-Javadoc)
         * @see com.googlecode.gwtphonegap.showcase.client.notification.NotificationPresenter.Display#getConfirmButton()
         */
        @Override
        public HasClickHandlers getConfirmButton() {
                return confirmButton;
        }

}