import java.math.BigInteger;
import java.security.PublicKey;
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
		// public key is output addres
		validOutput(tx.getOutputs(), tx.getHash());
		validInputSignatures(tx);
		return true;
	}

	private boolean validOutput(ArrayList<Transaction.Output> outputs, byte[] hash) {
		for (int i = 0; i < outputs.size(); i++) {
			if (utxoPool.contains(new UTXO(hash, i)) == false) {
				return false;
			}
		}

		return true;
	}

	private boolean validInputSignatures(Transaction transaction) {
		ArrayList<Transaction.Input> inputs = transaction.getInputs();
		ArrayList<Transaction.Output> outputs = transaction.getOutputs();

		for (int i = 0; i < inputs.size(); i++) {
			Transaction.Input input = inputs.get(i);
			Transaction.Output output = outputs.get(i);

			if (!Crypto.verifySignature(
					(PublicKey) output.address, transaction.getRawDataToSign(i),
					input.signature)) {
				return false;
			}
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
		// IMPLEMENT THIS
		return null;
	}

}
