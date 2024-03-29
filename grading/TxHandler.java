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
		boolean isValid = false;

		isValid = validOutput(tx);
		if (isValid == false) {
			return isValid;
		}
		isValid = validInputSignatures(tx);
		if (isValid == false) {
			return isValid;
		}
		isValid = checkUtxoIsUsedOnce(tx.getInputs());
		if (isValid == false) {
			return isValid;
		}
		isValid = checkOutputValueIsPositive(tx);
		if (isValid == false) {
			return isValid;
		}
		isValid = checkSumOfIndexValue(tx);

		return isValid;

		// if (validOutput(tx.getInputs()) &&
		// validInputSignatures(tx) &&
		// checkUtxoIsUsedOnce(tx.getInputs()) &&
		// checkOutputValueIsPositive(tx.getOutputs()) &&
		// checkSumOfIndexValue(tx)) {
		// return true;
		// }
	}

	// nummer 1
	private boolean validOutput(Transaction transaction) {
		for (int i = 0; i < transaction.numInputs(); i++) {
			UTXO utxo = new UTXO(transaction.getInput(i).prevTxHash, transaction.getInput(i).outputIndex);
			if (!utxoPool.contains(utxo)) {
				return false;
			}
		}
		return true;
	}

	// nummer 2
	private boolean validInputSignatures(Transaction transaction) {
		for (int i = 0; i < transaction.numInputs(); i++) {
			Transaction.Input input = transaction.getInput(i);

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
	private boolean checkOutputValueIsPositive(Transaction transaction) {
		for (int i = 0; i < transaction.numOutputs(); i++) {
			Transaction.Output output = transaction.getOutput(i);
			if (output.value < 0) {
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
			UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
			Transaction.Output output = utxoPool.getTxOutput(utxo);

			inputValue += output.value;
		}

		for (Transaction.Output output : transaction.getOutputs()) {
			outputValue += output.value;
		}

		if (inputValue < outputValue) {
			return false;
		}
		return true;
	}

	/*
	 * Handles each epoch by receiving an unordered array of proposed
	 * transactions, checking each transaction for correctness,
	 * returning a mutually valid array of accepted transactions,
	 * and updating the current UTXO pool as appropriate.
	 */

	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		ArrayList<Transaction> validTxs = new ArrayList<>();
		for (Transaction t : possibleTxs) {
			if (isValidTx(t)) {
				validTxs.add(t);

				// remove utxo
				for (Transaction.Input input : t.getInputs()) {
					int outputIndex = input.outputIndex;
					byte[] prevTxHash = input.prevTxHash;
					UTXO utxo = new UTXO(prevTxHash, outputIndex);
					utxoPool.removeUTXO(utxo);
				}
				// add new utxo
				byte[] hash = t.getHash();
				for (int i = 0; i < t.numOutputs(); i++) {
					UTXO utxo = new UTXO(hash, i);
					utxoPool.addUTXO(utxo, t.getOutput(i));
				}
			}
		}
		Transaction[] validTxsArr = new Transaction[validTxs.size()];
		validTxsArr = validTxs.toArray(validTxsArr);
		return validTxsArr;
	}
}
