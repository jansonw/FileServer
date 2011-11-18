package com.cs456.client.client;
import com.cs456.client.client.MainPresenter.Display;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;


public class MainDisplay implements Display {

        private FlowPanel main;
        private Button button;

        public MainDisplay() {
                main = new FlowPanel();
                main.setStylePrimaryName("main");

                HTML html = new HTML("GWT Phonegap Showcase");
                html.setStyleName("title");
                main.add(html);

                main.add(new HTML("click on the following links to see the different features of gwt-phonegap <br/><br/>"));

                button = new Button("Exit App (Android only)");

                main.add(button);

        }

        @Override
        public Widget asWidget() {

                return main;
        }

        @Override
        public HasWidgets getContainer() {
                return main;
        }

        @Override
        public HasClickHandlers getExitButton() {
                return button;
        }

}