package de.sub.goobi.helper.exceptions;

public class WrongImportFileException extends AbstractGoobiException {
   private static final long serialVersionUID = 3257853198839724340L;

   public WrongImportFileException() {
      super();
   }

   public WrongImportFileException(String s) {
      super(s);
   }
}
