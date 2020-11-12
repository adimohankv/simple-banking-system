package banking;

public class Main {
    public static void main(String[] args) {
        String filename = "";
        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("-fileName")) {
                filename = args[i + 1];
                break;
            }
        }

        System.out.println(filename);

        DBOps dbops = new DBOps(filename);
        dbops.createTable();

        BankingUI bankingUI = new BankingUI(dbops);
        bankingUI.displayStartMenu();
    }
}