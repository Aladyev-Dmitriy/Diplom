package ru.netology.dailyTrip.tests;

import com.codeborne.selenide.logevents.SelenideLogger;
import com.github.javafaker.Faker;
import io.qameta.allure.*;
import io.qameta.allure.selenide.AllureSelenide;
import org.testng.annotations.*;
import ru.netology.dailyTrip.helpers.DataHelper;
import ru.netology.dailyTrip.helpers.DbHelper;
import ru.netology.dailyTrip.pages.DailyTripFormPage;

import java.util.List;
import java.util.Locale;

import static com.codeborne.selenide.Selenide.open;
import static org.testng.AssertJUnit.*;

@Epic("Frontend тестирование функционала Путешествие дня")
public class DailyTripFrontendTest {
    private static Faker faker = new Faker(Locale.ENGLISH);
    private static DataHelper.CardData user;
    private static DailyTripFormPage dailyTrip;
    private static List<DbHelper.PaymentEntity> payments;
    private static List<DbHelper.CreditRequestEntity> credits;
    private static List<DbHelper.OrderEntity> orders;

    @BeforeClass
    public void setupClass() {
        DbHelper.setDown();
        SelenideLogger.addListener("allure", new AllureSelenide()
                .screenshots(true).savePageSource(true));
    }

    @BeforeMethod
    public void setupMethod() {
        open("http://localhost:8080/");
        dailyTrip = new DailyTripFormPage();
    }

    @AfterMethod
    public void setDownMethod() {
        DbHelper.setDown();
    }

    @AfterClass
    public void setDownClass() {
        SelenideLogger.removeListener("allure");
    }

    @Feature("Покупка тура по карте")
    @Story("HappyPath")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void shouldHappyPathPay() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();

        payments = DbHelper.getPayments();
        credits = DbHelper.getCreditsRequest();
        orders = DbHelper.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());

        assertEquals(dailyTrip.getAmount() * 100, payments.get(0).getAmount());
        assertTrue(payments.get(0).getStatus().equalsIgnoreCase("approved"));
        assertEquals(payments.get(0).getTransaction_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Feature("Покупка тура по карте")
    @Story("SadPath")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void shouldSadPathPay() {
        user = DataHelper.getValidUserWithDeclinedCard();
        dailyTrip.clickPayButton();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationWithErrorNotification();

        payments = DbHelper.getPayments();
        credits = DbHelper.getCreditsRequest();
        orders = DbHelper.getOrders();
        assertEquals(1, payments.size());
        assertEquals(0, credits.size());
        assertEquals(1, orders.size());

        assertEquals(dailyTrip.getAmount() * 100, payments.get(0).getAmount());
        assertTrue(payments.get(0).getStatus().equalsIgnoreCase("declined"));
        assertEquals(payments.get(0).getTransaction_id(), orders.get(0).getPayment_id());
        assertNull(orders.get(0).getCredit_id());
    }

    @Feature("Покупка тура в кредит")
    @Story("HappyPath")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void shouldHappyPathCredit() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickCreditButton();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();

        payments = DbHelper.getPayments();
        credits = DbHelper.getCreditsRequest();
        orders = DbHelper.getOrders();
        assertEquals(0, payments.size());
        assertEquals(1, credits.size());
        assertEquals(1, orders.size());

        assertTrue(credits.get(0).getStatus().equalsIgnoreCase("approved"));
        assertEquals(credits.get(0).getBank_id(), orders.get(0).getPayment_id());
        assertEquals(credits.get(0).getId(), orders.get(0).getCredit_id());
    }

    @Feature("Покупка тура в кредит")
    @Story("SadPath")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void shouldSadPathCredit() {
        user = DataHelper.getValidUserWithDeclinedCard();
        dailyTrip.clickCreditButton();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationWithErrorNotification();

        payments = DbHelper.getPayments();
        credits = DbHelper.getCreditsRequest();
        orders = DbHelper.getOrders();
        assertEquals(0, payments.size());
        assertEquals(1, credits.size());
        assertEquals(1, orders.size());

        assertTrue(credits.get(0).getStatus().equalsIgnoreCase("declined"));
        assertEquals(credits.get(0).getBank_id(), orders.get(0).getPayment_id());
        assertEquals(credits.get(0).getId(), orders.get(0).getCredit_id());
    }

    @Feature("Покупка тура по карте")
    @Story("Переключение с формы кредита на форму покупки")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldImmutableInputValueFromCreditToPay() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickCreditButton();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.clickPayButton();
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
    }

    @Feature("Покупка тура в кредит")
    @Story("Переключение с формы оплаты на форму кредита")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldImmutableInputValueFromPayToCredit() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.clickCreditButton();
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
    }

    @Feature("Покупка тура по карте")
    @Story("Пустое поле номер карты")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldNotificationWithEmptyNumber() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        dailyTrip.insertingValueInForm("", user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue("", user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertNumberFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля номера карты без пробелов")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldSuccessWithNoSpacebarInNumber() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        dailyTrip.insertingValueInForm(DataHelper.deleteSpacebar(user.getNumber()), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля номера карты c пробелами вначале и в конце")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldSuccessWithStartEndSpacebarInNumber() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        dailyTrip.insertingValueInForm(" " + user.getNumber() + " ", user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("11 цифр в поле номера карты")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWith11DigitsInNumber() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var cardNumber = DataHelper.generateCardNumber(11);
        dailyTrip.insertingValueInForm(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertNumberFieldIsInvalidValue();
    }

    @Feature("Покупка тура по карте")
    @Story("12 цифр в поле номера карты")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldFailedWith12DigitsInNumber() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var cardNumber = DataHelper.generateCardNumber(12);
        dailyTrip.insertingValueInForm(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationWithErrorNotification();
    }

    @Feature("Покупка тура по карте")
    @Story("13 цифр в поле номера карты")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldFailedWith13DigitsInNumber() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var cardNumber = DataHelper.generateCardNumber(13);
        dailyTrip.insertingValueInForm(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationWithErrorNotification();
    }

    @Feature("Покупка тура по карте")
    @Story("18 цифр в поле номера карты")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void shouldFailedWith18DigitsInNumber() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var cardNumber = DataHelper.generateCardNumber(18);
        dailyTrip.insertingValueInForm(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationWithErrorNotification();
    }

    @Feature("Покупка тура по карте")
    @Story("19 цифр в поле номера карты")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldFailedWith19DigitsInNumber() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var cardNumber = DataHelper.generateCardNumber(19);
        dailyTrip.insertingValueInForm(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationWithErrorNotification();
    }

    @Feature("Покупка тура по карте")
    @Story("20 цифр в поле номера карты")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWith20DigitsInNumber() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var cardNumber = DataHelper.generateCardNumber(20);
        dailyTrip.insertingValueInForm(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertNumberFieldIsInvalidValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Невалидные символы в поле номера карты")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWithInvalidSymbolsInNumber() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var cardNumber = "ASFD ФЯВЫ @&#% _,';";
        dailyTrip.insertingValueInForm(cardNumber, user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue("", user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertNumberFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Пустое поле месяц")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldNotificationWithEmptyMonth() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        dailyTrip.insertingValueInForm(user.getNumber(), "", user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), "", user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertMonthFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля месяц одной цифрой")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldAddNullInMonth() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var month = DataHelper.generateDigit(1);
        dailyTrip.insertingValueInForm(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), "0" + month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля месяц 3 цифрами")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldDeleteThirdDigitInMonth() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var month = user.getMonth() + "0";
        dailyTrip.insertingValueInForm(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля месяц значением 00")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWith00InMonth() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var month = "00";
        dailyTrip.insertingValueInForm(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertMonthFieldIsInvalidValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля месяц значением 01")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldSuccessWith01InMonth() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var month = "01";
        dailyTrip.insertingValueInForm(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля месяц значением 12")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldSuccessWith12InMonth() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var month = "12";
        dailyTrip.insertingValueInForm(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля месяц значением 13")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWith13InMonth() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var month = "13";
        dailyTrip.insertingValueInForm(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertMonthFieldIsInvalidValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Невалидные символы в поле месяц")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWithInvalidSymbolsInMonth() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var month = "Z@";
        dailyTrip.insertingValueInForm(user.getNumber(), month, user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), "", user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertMonthFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Пустое поле год")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldNotificationWithEmptyYear() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), "", user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), "", user.getHolder(), user.getCvc());
        dailyTrip.assertYearFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля год одной цифрой")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWithOneDigitInYear() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var year = DataHelper.generateDigit(1);
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), year, user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), year, user.getHolder(), user.getCvc());
        dailyTrip.assertYearFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля год 3 цифрами")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldDeleteThirdDigitInYear() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var year = user.getYear() + "0";
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), year, user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля год 4 цифрами")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldDeleteThirdFourDigitInYear() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var year = "20" + user.getYear();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), year, user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), "20", user.getHolder(), user.getCvc());
        dailyTrip.assertYearFieldIsInvalidValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля год предыдущим годом")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWithPrevYear() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var year = DataHelper.generateYear(-1);
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), year, user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), year, user.getHolder(), user.getCvc());
        dailyTrip.assertYearFieldIsInvalidValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля год текущим годом, поля месяц предыдущим")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWithPrevMonth() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var month = DataHelper.generateMonth(-1);
        var year = DataHelper.generateYear(0);
        dailyTrip.insertingValueInForm(user.getNumber(), month, year, user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), month, year, user.getHolder(), user.getCvc());
        dailyTrip.assertMonthFieldIsInvalidValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля год текущим годом, поля месяц текущим")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldSuccessWithCurrentMonth() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var month = DataHelper.generateMonth(0);
        var year = DataHelper.generateYear(0);
        dailyTrip.insertingValueInForm(user.getNumber(), month, year, user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), month, year, user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Заполнение поля год на пять лет вперед, поля месяц текущим")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldSuccessWithPlus5Year() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var month = DataHelper.generateMonth(0);
        var year = DataHelper.generateYear(5);
        dailyTrip.insertingValueInForm(user.getNumber(), month, year, user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), month, year, user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Невалидные символы в поле год")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWithInvalidSymbolsInYear() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var year = "Z@";
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), year, user.getHolder(), user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), "", user.getHolder(), user.getCvc());
        dailyTrip.assertYearFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Пустое поле владелец")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldNotificationWithEmptyHolder() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), "", user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), "", user.getCvc());
        dailyTrip.assertHolderFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Дефис в поле владелец")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldSuccessWithHyphenInHolder() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var holder = user.getHolder() + "-" + faker.name().lastName().toUpperCase();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), holder, user.getCvc());
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), holder, user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Нижний регистр в поле владелец")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldAutoUpperCaseInHolder() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var holder = user.getHolder().toLowerCase();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), holder, user.getCvc());
        assertEquals(user.getHolder(), dailyTrip.getHolder());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Пробелы вначале и в конце поля владелец")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldAutoDeleteStartEndHyphenInHolder() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var holder = " " + user.getHolder() + " ";
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), holder, user.getCvc());
        assertEquals(user.getHolder(), dailyTrip.getHolder());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Дефисы вначале и в конце поля владелец")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldAutoDeleteStartEndSpacebarInHolder() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var holder = "-" + user.getHolder() + "-";
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), holder, user.getCvc());
        assertEquals(user.getHolder(), dailyTrip.getHolder());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Кириллица в поле владелец")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void shouldNotificationWithCyrillicInHolder() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var holder = DataHelper.generateInvalidHolderWithCustomLocale(new Locale("ru", "RU"));
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), holder, user.getCvc());
        assertEquals("", dailyTrip.getHolder());
        dailyTrip.assertHolderFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Невалидные символы в поле владелец")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWithInvalidSymbolInHolder() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var holder = "123 @%# ;',/";
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), holder, user.getCvc());
        assertEquals("", dailyTrip.getHolder());
        dailyTrip.assertHolderFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("Пустое поле CVC/CVV")
    @Severity(SeverityLevel.NORMAL)
    @Test
    public void shouldNotificationWithEmptyCVC() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), "");
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), "");
        dailyTrip.assertCvcFieldIsEmptyValue();
    }

    @Feature("Покупка тура по карте")
    @Story("2 цифры в поле CVC/CVV")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWith2DigitsInCVC() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var cvc = DataHelper.generateValidCVC(2);
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), cvc);
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), cvc);
        dailyTrip.assertCvcFieldIsInvalidValue();
    }

    @Feature("Покупка тура по карте")
    @Story("4 цифры в поле CVC/CVV")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldSuccessWith4DigitsInCVC() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var cvc = user.getCvc() + DataHelper.generateValidCVC(1);
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), cvc);
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), user.getCvc());
        dailyTrip.assertBuyOperationIsSuccessful();
    }

    @Feature("Покупка тура по карте")
    @Story("Невалидные символы в поле CVC/CVV")
    @Severity(SeverityLevel.MINOR)
    @Test
    public void shouldNotificationWithInvalidSymbolsInCVC() {
        user = DataHelper.getValidUserWithApprovedCard();
        dailyTrip.clickPayButton();
        var cvc = "ZЯ$";
        dailyTrip.insertingValueInForm(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), cvc);
        dailyTrip.matchesByInsertValue(user.getNumber(), user.getMonth(), user.getYear(), user.getHolder(), "");
        dailyTrip.assertCvcFieldIsEmptyValue();
    }
}
