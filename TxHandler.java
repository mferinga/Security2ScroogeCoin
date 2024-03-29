import java.util.ArrayList;

public class TxHandler {

	/*
	 * Creates a public ledger whose current UTXOPool (collection of unspent
	 * transaction outputs) is utxoPool. This should make a defensive copy of
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	private UTXOPool utxoPool;

	public TxHandler(UTXOPool utxoPool) {
		this.utxoPool = utxoPool;
	}

	/*
	 * Returns true if
	 * (1) all outputs claimed by tx are in the current UTXO pool,
	 * (2) the signatures on each input of tx are valid,
	 * (3) no UTXO is claimed multiple times by tx,
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of
	 * its output values;
	 * and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		if (validOutput(tx.getInputs()) &&
				validInputSignatures(tx) &&
				checkUtxoIsUsedOnce(tx.getInputs()) &&
				checkOutputValueIsPositive(tx.getOutputs()) &&
				checkSumOfIndexValue(tx)) {
			return true;
		}
		return false;
	}

	// nummer 1
	private boolean validOutput(ArrayList<Transaction.Input> inputs) {
		for (int i = 0; i < inputs.size(); i++) {
			UTXO utxo = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
			if (!utxoPool.contains(utxo)) {
				return false;
			}
		}
		return true;
	}

	// nummer 2
	private boolean validInputSignatures(Transaction transaction) {
		ArrayList<Transaction.Input> inputs = transaction.getInputs();

		for (int i = 0; i < inputs.size(); i++) {
			Transaction.Input input = inputs.get(i);

			UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
			if (!utxoPool.getTxOutput(utxo).address.verifySignature(transaction.getRawDataToSign(i), input.signature)) {
				return false;
			}
		}
		return true;
	}

	// nummer 3
	private boolean checkUtxoIsUsedOnce(ArrayList<Transaction.Input> inputs) {
		ArrayList<UTXO> utxos = new ArrayList<>();
		for (int i = 0; i < inputs.size(); i++) {
			UTXO utxo = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
			if (utxos.contains(utxo)) {
				return false;
			}
			utxos.add(utxo);
		}
		return true;
	}

	// nummer 4
	private boolean checkOutputValueIsPositive(ArrayList<Transaction.Output> outputs) {
		for (Transaction.Output output : outputs) {
			if (output.value <= 0) {
				return false;
			}
		}
		return true;
	}

	// nummer 5
	private boolean checkSumOfIndexValue(Transaction transaction) {
		double inputValue = 0;
		double outputValue = 0;
		for (Transaction.Input input : transaction.getInputs()) {
			int outputIndex = input.outputIndex;
			if (transaction.getOutput(outputIndex) != null) {
				inputValue += transaction.getOutput(outputIndex).value;
			}
		}

		for (Transaction.Output output : transaction.getOutputs()) {
			outputValue += output.value;
		}

		return inputValue >= outputValue;
	}

	/*
	 * Handles each epoch by receiving an unordered array of proposed
	 * transactions, checking each transaction for correctness,
	 * returning a mutually valid array of accepted transactions,
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		ArrayList<Transaction> validTransactions = new ArrayList<>();
		for (Transaction transaction : possibleTxs) {
			if (isValidTx(transaction)) {
				validTransactions.add(transaction);

				for (Transaction.Input input : transaction.getInputs()) {
					UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
					utxoPool.removeUTXO(utxo);
				}

				byte[] hash = transaction.getHash();
				for (int i = 0; i < transaction.numOutputs(); i++) {
					UTXO utxo = new UTXO(hash, i);
					utxoPool.addUTXO(utxo, transaction.getOutput(i));
				}
			}
		}
		return validTransactions.toArray(new Transaction[validTransactions.size()]);
	}
}
