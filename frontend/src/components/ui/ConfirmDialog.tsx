import { Modal } from './Modal';
import type { ModalProps } from './Modal';
import { Button } from './Button';
import { AlertTriangle } from 'lucide-react';
import { useState } from 'react';

export interface ConfirmDialogProps extends Omit<ModalProps, 'title' | 'children'> {
  title: string;
  description: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void | Promise<void>;
  danger?: boolean;
  confirmKeyword?: string;
}

export const ConfirmDialog = ({
  title,
  description,
  confirmText = 'Xác nhận',
  cancelText = 'Hủy',
  onConfirm,
  danger = false,
  confirmKeyword,
  ...props
}: ConfirmDialogProps) => {
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState('');

  const handleConfirm = async () => {
    try {
      setLoading(true);
      await onConfirm();
      props.onClose();
    } finally {
      setLoading(false);
    }
  };

  const isConfirmDisabled = confirmKeyword && keyword !== confirmKeyword;

  return (
    <Modal {...props} hideCloseButton className="sm:max-w-md">
      <div className="flex flex-col items-center text-center sm:items-start sm:text-left sm:flex-row gap-4 pt-2">
        <div className={`shrink-0 flex items-center justify-center w-10 h-10 rounded-full ${danger ? 'bg-red-50 text-red-500' : 'bg-indigo-50 text-indigo-500'}`}>
          <AlertTriangle className="w-6 h-6" />
        </div>
        <div className="flex-1 space-y-2">
          <h3 className="text-lg font-semibold text-gray-800">{title}</h3>
          <p className="text-sm text-gray-500">{description}</p>
          
          {confirmKeyword && (
            <div className="mt-4 text-left space-y-2">
              <label className="text-sm font-medium text-gray-700">
                Gõ <span className="font-bold select-all">{confirmKeyword}</span> để xác nhận:
              </label>
              <input
                type="text"
                className="w-full bg-transparent border border-gray-500/30 outline-none rounded-full py-2 px-4 text-sm focus:border-red-500 focus:ring-2 focus:ring-red-500/20"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
              />
            </div>
          )}
        </div>
      </div>
      <div className="mt-6 flex flex-col-reverse sm:flex-row justify-end gap-3">
        <Button variant="secondary" onClick={props.onClose} disabled={loading} fullWidth className="sm:w-auto">
          {cancelText}
        </Button>
        <Button 
          variant={danger ? 'danger' : 'primary'} 
          onClick={handleConfirm} 
          loading={loading}
          disabled={!!isConfirmDisabled}
          fullWidth 
          className="sm:w-auto"
        >
          {confirmText}
        </Button>
      </div>
    </Modal>
  );
};
