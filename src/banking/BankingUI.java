package banking;

import java.util.Random;
import java.util.Scanner;

public class BankingUI {
    DBOps dbops;
    Scanner scanner = new Scanner(System.in);
    Random random = new Random();
    static final String IIN = "400000";

    public BankingUI(DBOps dbOps) {
        this.dbops = dbOps;
    }

    public void startUp() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
        System.out.print(">");
    }

    public void displayStartMenu() {
        startUp();
        int response = scanner.nextInt();

        switch (response) {
            case 1:
                displayCreateAccount();
                break;
            case 2:
                displayLoginAccount();
                break;
            case 0:
                System.out.println("Bye!");
                scanner.close();
                System.exit(0);
                break;
            default:
                displayStartMenu();
        }
    }

    private int generateChecksum(String accountNumber) {
        int sum = 0;

        for (int i = 0; i < accountNumber.length(); i++) {
            int digit = Character.getNumericValue(accountNumber.charAt(i));

            if ((i + 1) % 2 != 0) {
                int doubleDigit = digit * 2;

                sum = doubleDigit > 9 ? sum + (doubleDigit - 9) : sum + doubleDigit;
            } else {
                sum += digit;
            }
        }

        int checksum = 10 - sum % 10;

        return checksum == 10 ? 0 : checksum;
    }

    private Boolean verifyLuhnAlgo(String cardNumber) {
        int checksum = Character.getNumericValue(cardNumber.charAt(cardNumber.length() - 1));
        String accountNumber = cardNumber.substring(0, cardNumber.length() - 1);

        int generatedChecksum = generateChecksum(accountNumber);

        if (checksum == generatedChecksum) {
            return true;
        }

        return false;
    }

    private void handleTransfer() {
        System.out.println("Transfer");
        System.out.println("Enter the card number");
        String cardNumber = Long.toString(scanner.nextLong());

        if (verifyLuhnAlgo(cardNumber)) {
            if (dbops.checkIfCardExists(cardNumber)) {
                transferMoney();
            } else {
                System.out.println("Such a card does not exist.");
            }
        } else {
            System.out.println("Probably you made mistake in the card number. Please try again!");
        }

        displayWelcomeMenu();
    }

    private void transferMoney() {
        System.out.println("Enter how much money you want to transfer:");
        int addMoney = scanner.nextInt();

        if (addMoney > dbops.getBalance()) {
            System.out.println("Not enough money!");
        } else {
            Boolean isTransferSuccess = dbops.transferMoney(addMoney);
            if (isTransferSuccess) {
                System.out.println("Success!");
            } else {
                System.out.println("Issue with transfering money try again");
            }
        }

        displayWelcomeMenu();
    }

    private void displayCreateAccount() {
        String accountNumber = Integer.toString(100000000 + random.nextInt(900000000));
        String completeAccountNumber = IIN + accountNumber;
        int checksum = generateChecksum(completeAccountNumber);

        String cardNumber = completeAccountNumber + checksum;
        String pin = String.format("%04d", random.nextInt(10000));

        dbops.insertCard(cardNumber, pin);

        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(pin);
        System.out.println();

        displayStartMenu();
    }

    private void displayLoginAccount(){
        System.out.println("Enter your card number:");
        System.out.print(">");
        String number = Long.toString(scanner.nextLong());

        System.out.println("Enter your PIN:");
        System.out.print(">");
        String pin = Integer.toString(scanner.nextInt());

        if(dbops.login(number, pin)) {
            
            System.out.println("You have successfully logged in!");
            displayWelcomeMenu();
        } else {
            System.out.println("Wrong card number or PIN!\n");
            displayStartMenu();
        }
    }

    public void welcome() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close Account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
        System.out.print(">");
    }

    public void displayWelcomeMenu() {
        welcome();
        int response = scanner.nextInt();

        switch(response) {
            case 1:
                System.out.printf("Balance:  %d\n", dbops.getBalance());
                displayWelcomeMenu();
                break;
            case 2:
                System.out.println("Enter income");
                int income = scanner.nextInt();

                Boolean isUpdated = dbops.addIncome(income);

                if (isUpdated) {
                    System.out.println("Income was added!");
                } else {
                    System.out.println("Some issue with adding income!, try again");
                }

                displayWelcomeMenu();
                break;
            case 3:
                handleTransfer();
                break;
            case 4:
                Boolean isDeleted = dbops.deleteAccount();
                
                if (isDeleted) {
                    System.out.println("The account has been closed!");
                    displayStartMenu();
                }
                break;
            case 5:
                displayStartMenu();
            case 0:
                System.exit(0);
                break;
            default:
                displayWelcomeMenu();
        }
    }

}
