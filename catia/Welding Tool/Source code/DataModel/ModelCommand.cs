using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace Welding_Tool.ViewModel
{
    public class DelegateCommand : ICommand
    {
        private readonly Action _execute;
        private readonly Func<bool> _canExecute;

        public DelegateCommand(Func<bool> canExecute,  Action execute)
        {
            _canExecute = canExecute;
            _execute = execute;
        }

        public event EventHandler CanExecuteChanged;
        public void NotifyCanExecuteChanged()
        {
            CanExecuteChanged?.Invoke(this, EventArgs.Empty);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="parameter"></param>
        /// <returns></returns>
        bool ICommand.CanExecute(object parameter)
        {
            return _canExecute();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="parameter"></param>
        void ICommand.Execute(object parameter)
        {
            _execute();
        }

        /// <summary>
        /// 
        /// </summary>
        public bool CanExecute
        {
            get { return _canExecute(); }
        }
    }

    public class DelegateCommandWithParam : ICommand
    {
        private readonly Action<object> _execute;
        private readonly Func<bool> _canExecute;

        public DelegateCommandWithParam(Func<bool> canExecute, Action<object> execute)
        {
            _canExecute = canExecute;
            _execute = execute;
        }

        public event EventHandler CanExecuteChanged;
        public void NotifyCanExecuteChanged()
        {
            CanExecuteChanged?.Invoke(this, EventArgs.Empty);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="parameter"></param>
        /// <returns></returns>
        bool ICommand.CanExecute(object parameter)
        {
            return _canExecute();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="parameter"></param>
        void ICommand.Execute(object parameter)
        {
            _execute(parameter);
        }

        /// <summary>
        /// 
        /// </summary>
        public bool CanExecute
        {
            get { return _canExecute(); }
        }
    }
}
