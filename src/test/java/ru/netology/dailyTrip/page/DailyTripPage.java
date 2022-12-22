package ru.netology.dailyTrip.page;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class DailyTripPage {
    private SelenideElement dailyTripHeading = $x("//div[@id='root']/div/h2");
    private SelenideElement dailyTripCard = $x("//div[@id='root']/div/div[contains(@class, 'card')]");

    private SelenideElement payButton = $x("//span[text()='Купить']//ancestor::button");
    private SelenideElement creditButton = $x("//span[text()='Купить в кредит']//ancestor::button");

    private SelenideElement formHeading = $x("//form//preceding-sibling::h3");
    private SelenideElement form = $x("//form");
    private SelenideElement numberLabel = form.$x(".//span[text()='Номер карты']//ancestor::div/span");
    private SelenideElement numberInput = numberLabel.$x(".//ancestor::span//input");
    private SelenideElement monthLabel = form.$x(".//span[text()='Месяц']//ancestor::div/span/span[1]/span");
    private SelenideElement monthInput = monthLabel.$x(".//input");
    private SelenideElement yearLabel = form.$x(".//span[text()='Год']//ancestor::div/span/span[2]/span");
    private SelenideElement yearInput = yearLabel.$x(".//input");
    private SelenideElement holderLabel = form.$x(".//span[text()='Владелец']//ancestor::div/span/span[1]/span");
    private SelenideElement holderInput = holderLabel.$x(".//input");
    private SelenideElement cvcLabel = form.$x(".//span[text()='CVC/CVV']//ancestor::div/span/span[2]/span");
    private SelenideElement cvcInput = cvcLabel.$x(".//input");
    private SelenideElement continuousButton = form.$x(".//span[text()='Продолжить']//ancestor::button");

    private SelenideElement successNotification = $x("//div[contains(@class, 'notification_status_ok')]");
    private SelenideElement successCloseButton = successNotification.$x("./button");
    private SelenideElement errorNotification = $x("//div[contains(@class, 'notification_status_error')]");
    private SelenideElement errorCloseButton = errorNotification.$x("./button");

    public DailyTripPage() {
        dailyTripHeading.should(Condition.visible, Condition.text("Путешествие дня"));
        dailyTripCard.should(Condition.visible);

        payButton.should(Condition.visible);
        creditButton.should(Condition.visible);

        form.should(Condition.hidden);
        successNotification.should(Condition.hidden);
        errorNotification.should(Condition.hidden);
    }

    public void clickPayButton() {
        payButton.click();
        formHeading.should(Condition.visible, Condition.text("Оплата по карте"));
        form.should(Condition.visible);
    }

    public void clickPayButtonUseKeyboard() {
        dailyTripCard.pressTab();
        payButton.should(Condition.focused).pressEnter();
        formHeading.should(Condition.visible, Condition.text("Оплата по карте"));
        form.should(Condition.visible);
    }

    public void clickCreditButton() {
        creditButton.click();
        formHeading.should(Condition.visible, Condition.text("Кредит по данным карты"));
        form.should(Condition.visible);
    }

    public void clickCreditButtonUseKeyboard() {
        dailyTripCard.pressTab().pressTab();
        creditButton.should(Condition.focused).pressEnter();
        formHeading.should(Condition.visible, Condition.text("Кредит по данным карты"));
        form.should(Condition.visible);
    }

    public void insert(String number, String month, String year, String holder, String cvc) {
        numberLabel.click();
        numberInput.val(number);
        monthLabel.click();
        monthInput.val(month);
        yearLabel.click();
        yearInput.val(year);
        holderLabel.click();
        holderInput.val(holder);
        cvcLabel.click();
        cvcInput.val(cvc);
        continuousButton.click();
    }

    public void insertUseKeyboard(String number, String month, String year, String holder, String cvc) {
        numberLabel.shouldNot(Condition.cssClass("input_focused"));
        monthLabel.shouldNot(Condition.cssClass("input_focused"));
        yearLabel.shouldNot(Condition.cssClass("input_focused"));
        holderLabel.shouldNot(Condition.cssClass("input_focused"));
        cvcLabel.shouldNot(Condition.cssClass("input_focused"));

        creditButton.pressTab();
        numberLabel.should(Condition.cssClass("input_focused"));
        numberInput.val(number).pressTab();
        monthLabel.should(Condition.cssClass("input_focused"));
        monthInput.val(month).pressTab();
        yearLabel.should(Condition.cssClass("input_focused"));
        yearInput.val(year).pressTab();
        holderLabel.should(Condition.cssClass("input_focused"));
        holderInput.val(holder).pressTab();
        cvcLabel.should(Condition.cssClass("input_focused"));
        cvcInput.val(cvc).pressTab();
        continuousButton.should(Condition.focused).pressEnter();
    }

    public void matchesInputValue(String number, String month, String year, String holder, String cvc) {
        numberInput.should(Condition.value(number));
        monthInput.should(Condition.value(month));
        yearInput.should(Condition.value(year));
        holderInput.should(Condition.value(holder));
        cvcInput.should(Condition.value(cvc));
    }

    public void success() {
        successNotification.should(Condition.visible, Duration.ofSeconds(15));
        successNotification.should(Condition.cssClass("notification_visible"));
        successNotification.$x("./div[@class='notification__title']").should(Condition.text("Успешно"));
        successNotification.$x("./div[@class='notification__content']").should(Condition.text("Операция одобрена Банком."));
        successCloseButton.click();
        successNotification.should(Condition.hidden);
    }

    public void error() {
        errorNotification.should(Condition.visible, Duration.ofSeconds(15));
        errorNotification.should(Condition.cssClass("notification_visible"));
        errorNotification.$x("/div[@class='notification__title']").should(Condition.text("Ошибка"));
        errorNotification.$x("/div[@class='notification__content']").should(Condition.text("Ошибка! Банк отказал в проведении операции."));
        errorCloseButton.click();
        errorNotification.should(Condition.hidden);
    }
}
